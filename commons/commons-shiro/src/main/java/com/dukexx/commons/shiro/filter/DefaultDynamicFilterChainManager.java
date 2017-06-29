package com.dukexx.commons.shiro.filter;

import com.dukexx.commons.shiro.commons.collection.ReadWriteMap;
import lombok.Getter;
import lombok.Setter;
import org.apache.shiro.util.CollectionUtils;
import org.apache.shiro.web.filter.mgt.DefaultFilterChainManager;
import org.apache.shiro.web.filter.mgt.NamedFilterList;
import org.apache.shiro.web.filter.mgt.PathMatchingFilterChainResolver;
import org.apache.shiro.web.servlet.AbstractShiroFilter;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * not achieve
 * Created by dukexx on 2017/4/13.
 */
public class DefaultDynamicFilterChainManager implements DynamicFilterChainManager {

    private ReentrantReadWriteLock.WriteLock writeLock;

    private PathMatchingFilterChainResolver filterChainResolver;

    private DefaultFilterChainManager filterChainManager;
    //filterChain from before config
    private Map<String, NamedFilterList> defaultFilterChain;

    @Getter
    @Setter
    private AbstractShiroFilter shiroFilter;

    @Getter
    @Setter
    private FilterChainDao filterChainDao;

    public DefaultDynamicFilterChainManager(AbstractShiroFilter shiroFilter) {
        this.shiroFilter = shiroFilter;
    }

    public DefaultDynamicFilterChainManager(AbstractShiroFilter shiroFilter, FilterChainDao filterChainDao) {
        this(shiroFilter);
        this.filterChainDao = filterChainDao;
    }

    /**
     * init and load filterChain if FilterChainDao is not null
     */
    public void init() {
        filterChainResolver = (PathMatchingFilterChainResolver) shiroFilter.getFilterChainResolver();
        filterChainManager = (DefaultFilterChainManager) filterChainResolver.getFilterChainManager();

        //save default filterChain
        defaultFilterChain = new LinkedHashMap<>(filterChainManager.getFilterChains());
        // map
        ReadWriteMap<String, NamedFilterList> synMap = new ReadWriteMap<String, NamedFilterList>(filterChainManager.getFilterChains());
        writeLock = synMap.getWriteLock();
        filterChainManager.setFilterChains(synMap);
        //on load
        if (filterChainDao != null) {
            reloadFilterChains(FilterOrder.TOP);
        }
    }

    /**
     * load chains from arguments but not remove filterChains from other config
     *
     * @param filterChainConfs
     * @param filterOrder
     */
    @Override
    public void reloadFilterChains(Collection<FilterChainConf> filterChainConfs, FilterOrder filterOrder) {
        if (filterChainConfs == null) {
            throw new IllegalArgumentException("filterChainConfs cannot be null when reloadFilterChains");
        }
        writeLock.lock();
        try {
            //clear old filterChains
            Map<String, NamedFilterList> filterChains = filterChainManager.getFilterChains();
            //default top
            filterChains.clear();
            if (!FilterOrder.BOTTOM.equals(filterOrder)) {
                putToChains(filterChainConfs);
                filterChains.putAll(defaultFilterChain);
            } else {
                putToChains(filterChainConfs);
                filterChains.putAll(defaultFilterChain);
            }
        } finally {
            writeLock.unlock();
        }

    }

    /**
     * load chains from source but not remove filterChains from other config
     *
     * @param filterOrder
     */
    @Override
    public void reloadFilterChains(FilterOrder filterOrder) {
        if (filterChainDao == null) {
            throw new IllegalArgumentException("filterChainDao cannot be null when reloadFilterChains");
        }
        Collection<FilterChainConf> filterChainConfs = filterChainDao.readFilterChainConfs();
        if (!CollectionUtils.isEmpty(filterChainConfs)) {
            reloadFilterChains(filterChainConfs, filterOrder);
        }
    }

    /**
     * add if url not existed or update if url existed chain from arguments
     * 暂时不可用, 未实现PathMatchingFilter中的appliedPaths修改, 会导致内存不断增大且无法释放
     *
     * @param filterChainConfs
     * @param filterOrder
     */
    @Deprecated
    @Override
    public void putFilterChains(Collection<FilterChainConf> filterChainConfs, FilterOrder filterOrder) {
        Map filterChains = filterChainManager.getFilterChains();
        Map oldFilterChains = new LinkedHashMap(filterChains);
        writeLock.lock();
        try {
            //default top
            if (!FilterOrder.BOTTOM.equals(filterOrder)) {
                filterChains.clear();
                putToChains(filterChainConfs);
                filterChains.putAll(oldFilterChains);
            }
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * get url-FilterChainConf map
     * 暂时不可用, 未实现, 涉及到filter中的appliedPaths中的配置, 即chainSpecificFilterConfig, 即[]中的split后的内容
     *
     * @return
     */
    @Deprecated
    @Override
    public Map<String, FilterChainConf> getFilterChains() {
        //not achieve
        return null;
    }

    /**
     * delete filterChains by urls
     * 暂时不可用, 未实现PathMatchingFilter中的appliedPaths修改, 会导致内存无法释放
     *
     * @param urls
     */
    @Deprecated
    @Override
    public void deleteFilterChains(Collection<String> urls) {
        //not achieve
    }

    /**
     * put to chains
     *
     * @param filterChainConfs
     */
    protected void putToChains(Collection<FilterChainConf> filterChainConfs) {
        //clear old filterChains
        Map<String, NamedFilterList> filterChains = filterChainManager.getFilterChains();
        filterChains.clear();
        for (FilterChainConf conf : filterChainConfs) {
            checkFilterChainConf(conf);
            filterChainManager.addToChain(conf.getUrl(), conf.getFilterName(), conf.getFilterConfString());
        }
    }

    /**
     * check FilterChainConf
     *
     * @param filterChainConf
     */
    protected void checkFilterChainConf(FilterChainConf filterChainConf) {
        if (filterChainConf != null) {
            if (filterChainConf.getUrl() == null) {
                throw new IllegalArgumentException("url of filterChainConf cannot be null");
            }
            if (filterChainConf.getFilterName() == null) {
                filterChainConf.setFilterName("anon");
            }
        }
    }
}
