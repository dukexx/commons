package com.dukexx.boot.shiro.autoconfigure;

import com.dukexx.boot.shiro.redis.SpringTemplateRedisDao;
import com.dukexx.boot.shiro.session.DefaultRedisSessionDao;
import com.dukexx.boot.shiro.redis.RedisDao;
import com.dukexx.boot.shiro.properties.ShiroProperties;
import com.dukexx.boot.shiro.properties.ShiroRedisProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.session.mgt.eis.SessionDAO;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * default support redis SessionDao
 *
 * @author dukexx
 * @date 2017/4/14
 * @since 1.0.0
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(ShiroProperties.class)
@AutoConfigureBefore(ShiroAutoConfiguration.class)
@ConditionalOnProperty(prefix = "shiro.session", name = "type", havingValue = "redis")
public class ShiroRedisSessionAutoConfigration {

    /**
     * 当shiro.session_type配置为redis时，且没有sessionDAO bean存在时创建
     * 必须存在RedisDao
     *
     * @param redisDao
     * @param properties
     * @return
     */
    @Bean
    @ConditionalOnMissingBean(SessionDAO.class)
    public SessionDAO defaultRedisSessionDao(RedisDao redisDao, ShiroProperties properties) {
        log.info("shiro.session.type config is redis but not found bean defined of type org.apache.shiro.session.mgt.eis.SessionDAO, " +
                "use default DefaultRedisSessionDao");
        DefaultRedisSessionDao defaultRedisSessionDao = new DefaultRedisSessionDao(redisDao);
        defaultRedisSessionDao.setSessionPrefix(properties.getSession().getPrefix().trim());
        return defaultRedisSessionDao;
    }

    /**
     * 默认RedisDao实现,当shiro.session_type配置为redis时，且没有RedisDao bean存在时创建
     *
     * @return
     */
    @Bean
    @ConditionalOnMissingBean(RedisDao.class)
    public RedisDao defaultSpringRedisDao(ShiroProperties properties, ApplicationContext applicationContext) {
        String ref = properties.getRedisTemplate().getRef();
        RedisTemplate redisTemplate = null;
        if (ShiroRedisProperties.DEFAULT_REF.equals(ref)) {
            redisTemplate = ConfigurationHelper.getSingletonRedisTemplate(properties.getRedisTemplate());
        } else if (ref != null) {
            log.info("use custom RedisTemplate bean:{}", ref);
            redisTemplate = (RedisTemplate) applicationContext.getBean(ref);
        } else {
            try {
                redisTemplate = applicationContext.getBean(RedisTemplate.class);
                log.info("use RedisTemplate from applicationContext:{}", redisTemplate);
            } catch (BeansException e) {
                log.info("not found RedisTemplate from applicationContext, use default RedisTemplate");
                redisTemplate = ConfigurationHelper.getSingletonRedisTemplate(properties.getRedisTemplate());
            }
        }
        return new SpringTemplateRedisDao(redisTemplate);
    }

}
