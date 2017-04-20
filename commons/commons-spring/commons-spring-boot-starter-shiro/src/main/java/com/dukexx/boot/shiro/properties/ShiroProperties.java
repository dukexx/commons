package com.dukexx.boot.shiro.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Created by dukexx on 2017/4/8.
 *
 * shiro基本属性配置
 * shiro.type: 应用类型，可以为javase，web，如果为javase，则不会应用web相关的配置，也不会注册ShiroFilterFactoryBean。
 * shiro.useCache: 是否启用缓存，为true时，容器中的CacheManager会生效，如果没有定义，则使用默认的 MemoryConstrainedCacheManager。
 * shiro.session: session相关配置。
 * shiro.filter: filter相关配置，仅在shiro.type为web时有效。
 * shiro.redisTemplate: spring RedisTemplate的配置，仅在shiro.session.type为redis时，应用没有提供SessionDao实现时生效。
 */
@Data
@ConfigurationProperties(prefix = ShiroProperties.PREFIX)
public class ShiroProperties {
    public static final String PREFIX = "shiro";

    //指定应用类型，默认web，只支持javase和web，如果是javase，SecurityManager和SessionManager都是非web的，且不会注册过滤器
    private Type type = Type.WEB;
    //是否使用缓存，即缓存管理器
    private ShiroCacheProperties cache = new ShiroCacheProperties();
    //session配置
    private ShiroSessionProperties session = new ShiroSessionProperties();
    //过滤器配置
    private ShiroFilterProperties filter = new ShiroFilterProperties();
    //redis配置，使用默认redis时的配置
    private ShiroRedisProperties redisTemplate = new ShiroRedisProperties();

    /**
     * 应用类型
     */
    public enum Type {
        WEB, JAVASE
    }



}
