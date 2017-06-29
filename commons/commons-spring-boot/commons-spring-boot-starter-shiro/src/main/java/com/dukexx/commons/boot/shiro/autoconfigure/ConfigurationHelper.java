package com.dukexx.commons.boot.shiro.autoconfigure;

import com.dukexx.commons.boot.shiro.exception.IllegalShiroConfigException;
import com.dukexx.commons.boot.shiro.properties.ShiroRedisProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @author dukexx
 * @date 2017/4/14
 * @since 1.0.0
 */
@Slf4j
public class ConfigurationHelper {

    private static RedisTemplate redisTemplate;

    /**
     * 获取单例RedisTemplate
     *
     * @param properties
     * @return
     */
    public static RedisTemplate getSingletonRedisTemplate(ShiroRedisProperties properties) {
        if (redisTemplate == null) {
            redisTemplate = defaultRedisTemplate(properties);
        }
        return redisTemplate;
    }

    /**
     * 获取非单例RedisTemplate
     *
     * @param properties
     * @return
     */
    public static RedisTemplate getPrototypeRedisTemplate(ShiroRedisProperties properties) {
        return defaultRedisTemplate(properties);
    }

    /**
     * 简单默认RedisTemplate实现，不会注册到applicationContext
     *
     * @param shiroRedisProperties
     * @return
     */
    public static RedisTemplate defaultRedisTemplate(ShiroRedisProperties shiroRedisProperties) {
        log.info("not found bean defined of type org.springframework.data.redis.core.RedisTemplate, use default DefaultRedisTemplate");
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        Integer port = shiroRedisProperties.getPort();
        String host = shiroRedisProperties.getHost();
        if (port == null || host == null) {
            String msg = "illegal config of shiro.redis.port or host, port and host cannot be null";
            IllegalShiroConfigException illegalConfigException = new IllegalShiroConfigException(msg);
            log.error(msg, illegalConfigException);
            throw illegalConfigException;
        }
        jedisPoolConfig.setMaxTotal(shiroRedisProperties.getMaxTotal());
        jedisPoolConfig.setMinIdle(shiroRedisProperties.getMinIdle());
        jedisPoolConfig.setMaxWaitMillis(shiroRedisProperties.getMaxWaitMillis());
        JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory();
        jedisConnectionFactory.setPort(port);
        jedisConnectionFactory.setHostName(host);
        jedisConnectionFactory.setTimeout(shiroRedisProperties.getTimeout());
        String password = shiroRedisProperties.getPassword();
        if (password != null && !password.trim().isEmpty()) {
            jedisConnectionFactory.setPassword(shiroRedisProperties.getPassword());
        }
        jedisConnectionFactory.setPoolConfig(jedisPoolConfig);
        jedisConnectionFactory.afterPropertiesSet();
        RedisTemplate redisTemplate = new RedisTemplate();
        redisTemplate.setConnectionFactory(jedisConnectionFactory);
        //initialized
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }
}
