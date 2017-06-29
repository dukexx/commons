package com.dukexx.commons.shiro.filter;

import java.util.Collection;
import java.util.Map;

/**
 * @author dukexx
 * @date 2017/4/17
 * @since 1.0.0
 */
public interface DynamicFilterChainManager {

    /**
     * load chains from source but not remove filterChains from other config
     * @param filterOrder
     */
    void reloadFilterChains(FilterOrder filterOrder);

    /**
     * load chains from arguments but not remove filterChains from other config
     * @param filterChainConfs
     * @param filterOrder
     */
    void reloadFilterChains(Collection<FilterChainConf> filterChainConfs, FilterOrder filterOrder);

    /**
     * add if url not existed or update if url existed chain from arguments
     * @param filterChainConfs
     * @param filterOrder
     */
    void putFilterChains(Collection<FilterChainConf> filterChainConfs, FilterOrder filterOrder);

    /**
     * get url-FilterChainConf map
     * @return
     */
    Map<String, FilterChainConf> getFilterChains();

    /**
     * delete filterChains by urls
     * @param urls
     */
    void deleteFilterChains(Collection<String> urls);

}
