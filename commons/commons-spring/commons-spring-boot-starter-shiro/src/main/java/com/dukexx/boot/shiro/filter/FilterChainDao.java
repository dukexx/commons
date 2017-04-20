package com.dukexx.boot.shiro.filter;

import java.util.Collection;

/**
 * @author dukexx
 * @date 2017/4/17
 * @since 1.0.0
 */
public interface FilterChainDao {

    Collection<FilterChainConf> readFilterChainConfs();
}
