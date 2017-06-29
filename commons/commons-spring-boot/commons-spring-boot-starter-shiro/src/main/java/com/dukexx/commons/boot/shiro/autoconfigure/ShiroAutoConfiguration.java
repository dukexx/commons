package com.dukexx.commons.boot.shiro.autoconfigure;

import com.dukexx.commons.shiro.cache.DefaultRedisTemplateCacheManager;
import com.dukexx.commons.shiro.filter.DynamicFilterChainManager;
import com.dukexx.commons.boot.shiro.properties.ShiroFilterProperties;
import com.dukexx.commons.shiro.filter.DefaultDynamicFilterChainManager;
import com.dukexx.commons.shiro.filter.FilterChainDao;
import com.dukexx.commons.boot.shiro.properties.ShiroCacheProperties;
import com.dukexx.commons.boot.shiro.properties.ShiroProperties;
import com.dukexx.commons.boot.shiro.properties.ShiroSessionProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.cache.MemoryConstrainedCacheManager;
import org.apache.shiro.cache.ehcache.EhCacheManager;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.session.mgt.DefaultSessionManager;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.session.mgt.eis.EnterpriseCacheSessionDAO;
import org.apache.shiro.session.mgt.eis.MemorySessionDAO;
import org.apache.shiro.session.mgt.eis.SessionDAO;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.AbstractShiroFilter;
import org.apache.shiro.web.servlet.Cookie;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.apache.shiro.web.session.mgt.ServletContainerSessionManager;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.MethodInvokingFactoryBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;
import java.util.HashMap;
import java.util.Map;

/**
 * @author dukexx
 * @date 2017/4/14
 * @since 1.0.0
 */
@SuppressWarnings("SpringJavaAutowiringInspection")
@Slf4j
@Configuration
@EnableConfigurationProperties(ShiroProperties.class)
public class ShiroAutoConfiguration implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    /**
     * shiro生命周期处理器
     *
     * @return
     */
    @Bean
    @ConditionalOnMissingBean   /*如果不设置value，默认为当前bean类型*/
    public LifecycleBeanPostProcessor lifecycleBeanPostProcessor() {
        return new LifecycleBeanPostProcessor();
    }

    /**
     * 默认SessionDAO，根据配置
     *
     * @param shiroProperties
     * @return
     */
    @Bean
    @ConditionalOnMissingBean(SessionDAO.class)
    public SessionDAO sessionDAO(ShiroProperties shiroProperties) {
        log.info("not found bean defined of type org.apache.defaults.session.mgt.eis.SessionDAO, use default SessionDAO");
        ShiroSessionProperties.SessionType type = shiroProperties.getSession().getType();
        SessionDAO sessionDAO = null;
        if (ShiroSessionProperties.SessionType.CACHE.equals(type)) {
            log.info("session_type config is cache, use EnterpriseCacheSessionDAO");
            sessionDAO = new EnterpriseCacheSessionDAO();
        } else if (ShiroSessionProperties.SessionType.DEFAULT.equals(type)) {
            log.info("session_type config is default, use MemorySessionDAO");
            sessionDAO = new MemorySessionDAO();
        }
        return sessionDAO;
    }

    /**
     * 获取SessionManager
     *
     * @param sessionDAO
     * @return
     */
    @Bean(name = "sessionManager")
    @ConditionalOnMissingBean(SessionManager.class)
    public SessionManager sessionManager(SessionDAO sessionDAO, ShiroProperties shiroProperties) {
        log.info("not found bean defined of type org.apache.defaults.session.mgt.SessionManager, use default SessionManager");
        SessionManager sessionManager = null;
        ShiroProperties.Type type = shiroProperties.getType();
        ShiroSessionProperties sessionProperties = shiroProperties.getSession();
        ShiroSessionProperties.SessionType sessionType = sessionProperties.getType();
        if (ShiroProperties.Type.WEB.equals(type)) {
            if (ShiroSessionProperties.SessionType.HTTP.equals(sessionType)) {
                log.info("session_type config is http, use ServletContainerSessionManager");
                sessionManager = new ServletContainerSessionManager();
                ServletContainerSessionManager servletContainerSessionManager = new ServletContainerSessionManager();
            } else {
                //使用shiro自带的session，不使用httpSession
                DefaultWebSessionManager defaultWebSessionManager = new DefaultWebSessionManager();
                //配置cookie模板
                defaultWebSessionManager.setSessionIdCookie(getSimpleCookie(sessionProperties));
                sessionManager = defaultWebSessionManager;
            }
        } else if (ShiroProperties.Type.JAVASE.equals(type)) {
            log.info("type config is javase, use DefaultSessionManager");
            DefaultSessionManager defaultSessionManager = new DefaultSessionManager();
            sessionManager = defaultSessionManager;
        }
        if (sessionManager instanceof DefaultSessionManager) {
            DefaultSessionManager defaultSessionManager = (DefaultSessionManager) sessionManager;
            defaultSessionManager.setSessionDAO(sessionDAO);
            //timeout
            defaultSessionManager.setGlobalSessionTimeout(sessionProperties.getTimeout());
            if (ShiroSessionProperties.SessionType.CACHE.equals(sessionType)) {
                defaultSessionManager.setCacheManager(getCacheManager(shiroProperties));
            }
        }
        return sessionManager;
    }

    /**
     * defaults securityManager
     * authorizingRealm是必须的，SessionManager如果不存在则根据配置使用默认
     *
     * @param authorizingRealm
     * @param sessionManager
     * @return
     */
    @Bean(name = "securityManager")
    @ConditionalOnMissingBean(SecurityManager.class)
    public SecurityManager securityManager(AuthorizingRealm authorizingRealm, SessionManager sessionManager,
                                           ShiroProperties shiroProperties) {
        log.info("not found bean defined of type org.apache.defaults.mgt.SecurityManager, use default SecurityManager");
        ShiroCacheProperties cacheProperties = shiroProperties.getCache();
        DefaultSecurityManager securityManager = null;
        ShiroProperties.Type type = shiroProperties.getType();
        if (ShiroProperties.Type.JAVASE.equals(type)) {
            securityManager = new DefaultSecurityManager();
        } else if (ShiroProperties.Type.WEB.equals(type)) {
            securityManager = new DefaultWebSecurityManager();
        }
        securityManager.setSessionManager(sessionManager);
        //设置Realm
        securityManager.setRealm(authorizingRealm);
        //设置CacheManager
        CacheManager cacheManager = null;
        if (cacheProperties.getOpenRealmCache() == null) {
            try {
                cacheManager = applicationContext.getBean(CacheManager.class);
            } catch (BeansException e) {
                log.info("shiro.cache.openRealmCache config not found and not found bean defined from applicationContext, " +
                        "so not open realmCache");
            }
        } else if (cacheProperties.getOpenRealmCache()) {
            cacheManager = getCacheManager(shiroProperties);
        }
        if (cacheManager != null) {
            securityManager.setCacheManager(cacheManager);
        }
        return securityManager;
    }

    /**
     * 开启注解权限支持
     *
     * @param securityManager
     * @return
     */
    @Bean
    @ConditionalOnMissingBean(AuthorizationAttributeSourceAdvisor.class)
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(SecurityManager securityManager) {
        AuthorizationAttributeSourceAdvisor advisor = new AuthorizationAttributeSourceAdvisor();
        advisor.setSecurityManager(securityManager);
        return advisor;
    }

    /**
     * MethodInvokingFactoryBean，spring自动执行方法，相当于调用了SecurityUtils.setSecurityManager方法
     *
     * @param securityManager
     * @return
     */
    @Bean
    @ConditionalOnBean(SecurityManager.class)
    public MethodInvokingFactoryBean methodInvokingFactoryBean(SecurityManager securityManager) {
        MethodInvokingFactoryBean methodInvokingFactoryBean = new MethodInvokingFactoryBean();
        methodInvokingFactoryBean.setStaticMethod("org.apache.shiro.SecurityUtils.setSecurityManager");
        methodInvokingFactoryBean.setArguments(new Object[]{securityManager});
        return methodInvokingFactoryBean;
    }

    /**
     * shiroFilter，配置过滤模式，如果shiro.type为web或默认(web)时创建
     *
     * @param securityManager
     * @return
     */
    @Bean(name = "shiroFilter")
    @ConditionalOnMissingBean(ShiroFilterFactoryBean.class)
    @ConditionalOnProperty(prefix = "shiro", name = "type", havingValue = "web", matchIfMissing = true)
    public ShiroFilterFactoryBean shiroFilterFactoryBean(SecurityManager securityManager, ShiroProperties shiroProperties) {
        log.info("shiro.type config is web but not found bean defined of type org.apache.shiro.spring.web.ShiroFilterFactoryBean, " +
                "use default ShiroFilterFactoryBean");
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        shiroFilterFactoryBean.setSecurityManager(securityManager);
        ShiroFilterProperties filterProperties = shiroProperties.getFilter();
        shiroFilterFactoryBean.setLoginUrl(filterProperties.getLoginUrl());
        shiroFilterFactoryBean.setUnauthorizedUrl(filterProperties.getUnauthUrl());
        shiroFilterFactoryBean.setSuccessUrl(filterProperties.getSuccessUrl());
        shiroFilterFactoryBean.setFilters(createFilterMap(filterProperties.getFilters()));
        String filterChain = filterProperties.getFilterChain();
        if (filterChain != null) {
            shiroFilterFactoryBean.setFilterChainDefinitions(filterChain);
        }
        Map<String, String> filterChainMap = filterProperties.getFilterChainMap();
        if (filterChainMap != null) {
            shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainMap);
        }
        return shiroFilterFactoryBean;
    }

    /**
     * DefaultDynamicFilterChainManager, dynamic load filterChains
     *
     * @param properties
     * @return
     */
    @Bean(name = "dynamicFilterChainManager", initMethod = "init")
    @ConditionalOnBean(ShiroFilterFactoryBean.class)
    @ConditionalOnMissingBean(DynamicFilterChainManager.class)
    @ConditionalOnProperty(prefix = "shiro.filter",name = "dynamicChain",havingValue = "true")
    public DynamicFilterChainManager dynamicFilterChainManager(ShiroProperties properties) {
        ShiroFilterProperties filterProperties = properties.getFilter();
        String chainDaoRef = filterProperties.getChainDaoRef();
        String filterRef = filterProperties.getFilterRef();
        FilterChainDao filterChainDao = null;
        AbstractShiroFilter shiroFilter = null;
        if (chainDaoRef != null) {
            filterChainDao = (FilterChainDao) applicationContext.getBean(chainDaoRef);
        } else {
            try {
                filterChainDao = applicationContext.getBean(FilterChainDao.class);
            } catch (BeansException e) {
                log.info("reference FilterChainDao failed, use null FilterChainDao, cause by: {}", e.getMessage());
            }
        }
        if (shiroFilter != null) {
            shiroFilter = (AbstractShiroFilter) applicationContext.getBean(filterRef);
        } else {
            shiroFilter = applicationContext.getBean(AbstractShiroFilter.class);
        }
        DynamicFilterChainManager dynamicFilterChainManager = new DefaultDynamicFilterChainManager(shiroFilter,filterChainDao);
        return dynamicFilterChainManager;
    }

    /**
     * 创建指定的CacheManager
     *
     * @return
     */
    protected CacheManager getCacheManager(ShiroProperties properties) {
        ShiroCacheProperties cacheProperties = properties.getCache();
        //by ref
        CacheManager cacheManager = null;
        String ref = cacheProperties.getRef();
        if (ShiroCacheProperties.DEFAULT_CACHE.equals(ref) || ref == null) {
            if (ref == null) {
                //from applicationContext
                try {
                    return applicationContext.getBean(CacheManager.class);
                } catch (BeansException e) {
                }
            }
            //default
            cacheManager = getDefaultCacheManager(properties);
        } else {
            cacheManager = (CacheManager) applicationContext.getBean(ref);
            log.info("use ref CacheManager:{} from applicationContext", ref);
        }
        return cacheManager;
    }

    /**
     * 根据配置生成默认CacheManager
     *
     * @param properties
     * @return
     */
    protected CacheManager getDefaultCacheManager(ShiroProperties properties) {
        ShiroCacheProperties cacheProperties = properties.getCache();
        ShiroCacheProperties.CacheType type = cacheProperties.getType();
        log.info("use default CacheManager by shiro.cache.type:{}", type);
        if (ShiroCacheProperties.CacheType.DEFAULT.equals(type)) {
            //default
            return new MemoryConstrainedCacheManager();
        } else if (ShiroCacheProperties.CacheType.EHCACHE.equals(type)) {
            //default EhCacheManager
            EhCacheManager ehCacheManager = new EhCacheManager();
            String ehcacheLocation = cacheProperties.getEhcacheLocation();
            if (ehcacheLocation != null) {
                ehCacheManager.setCacheManagerConfigFile(ehcacheLocation);
            }
            return ehCacheManager;
        } else {
            //redis cache:DefaultRedisTemplateCacheManager
            return new DefaultRedisTemplateCacheManager(ConfigurationHelper.getSingletonRedisTemplate(properties.getRedisTemplate()));
        }
    }

    /**
     * 创建cookie并设置属性
     *
     * @param sessionProperties
     * @return
     */
    protected Cookie getSimpleCookie(ShiroSessionProperties sessionProperties) {
        Cookie cookie = new SimpleCookie();
        cookie.setName(sessionProperties.getCookie());
        //cookie中单位为是s
        cookie.setMaxAge((int) (sessionProperties.getTimeout() / 1000));
        cookie.setHttpOnly(sessionProperties.isHttpOnly());
        return cookie;
    }

    /**
     * 根据ShiroFilterProperties，即shiro.filter配置的filters属性创建FilterMap
     *
     * @param filterClasss
     * @return
     */
    protected Map<String, Filter> createFilterMap(Map<String, Class> filterClasss) {
        Map<String, Filter> filterMap = new HashMap<>();
        if (filterClasss == null) {
            return filterMap;
        }
        for (Map.Entry<String, Class> entry : filterClasss.entrySet()) {
            try {
                filterMap.put(entry.getKey(), (Filter) entry.getValue().newInstance());
            } catch (InstantiationException | IllegalAccessException e) {
                log.error("create filter error", e);
                throw new RuntimeException(e);
            } catch (NullPointerException e) {
                log.error("create filter error, filter config is null or cannot find filter type", e);
                throw new IllegalArgumentException("filter config is null or cannot find filter type", e);
            }
        }
        return filterMap;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
