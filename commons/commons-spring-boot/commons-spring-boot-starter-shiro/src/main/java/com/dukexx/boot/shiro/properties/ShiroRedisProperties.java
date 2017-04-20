package com.dukexx.boot.shiro.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Created by dukexx on 2017/4/11.
 * spring RedisTemplate的配置，仅在shiro.session.type为redis时，应用没有提供SessionDao实现时生效。
 * shiro.redisTemplate.ref：引用的RedisTemplate bean名称；
 * 如果为null，则从applicationContext中获取RedisTemplate，如果应用没有提供，则默认RedisTemplate会被使用；
 * 如果为default，则使用默认的简单RedisTemplate；否则查找指定的bean。
 * 以上情况都不会将默认RedisTemplate注册为bean。
 */
@Data
@ConfigurationProperties(ShiroRedisProperties.PREFIX)
public class ShiroRedisProperties {
    public static final String PREFIX = "shiro.redisTemplate";
    public static final String DEFAULT_REF = "default";

    //引用的RedisTemplate名称，如果引用自定义RedisTemplate，其它配置即无效，如果是default，则使用自带的RedisTemplate
    private String ref;
    private Integer port;
    private String host;
    private Integer maxTotal = 1024;
    private Integer minIdle = 200;
    private Integer maxWaitMillis = 1000;
}
