package com.dukexx.commons.boot.shiro.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Created by dukexx on 2017/4/12.
 * shiro.cache.openRealmCache：仅用于设置Realm缓存，如果不设置值，则根据应用是否提供CacheManager
 */
@Data
@ConfigurationProperties(prefix = ShiroCacheProperties.PREFIX)
public class ShiroCacheProperties {

    public static final String PREFIX = "shiro.cache";

    public static final String DEFAULT_CACHE = "default";


    //是否启用Realm缓存管理器
    private Boolean openRealmCache;

    //默认的Cache类型，如果应用没有提供CacheManager时生效
    private CacheType type = CacheType.DEFAULT;

    //引用的CacheManager bean名称，用于在applicationContext中查找CacheManager，优先于type配置，如果为default，则使用type
    //指定的CacheManager

    private String ref;

    //如果使用Ehcache，指定配置文件名称
    private String ehcacheLocation;


    public enum CacheType {
        DEFAULT,EHCACHE,REDIS
    }
}
