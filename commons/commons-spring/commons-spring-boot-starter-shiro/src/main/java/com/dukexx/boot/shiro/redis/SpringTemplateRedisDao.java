package com.dukexx.boot.shiro.redis;

import org.springframework.data.redis.core.RedisTemplate;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author dukexx
 * @date 2017/4/14
 * @since 1.0.0
 */
public class SpringTemplateRedisDao implements RedisDao {

    //org.springframework.data.redis.core.RedisTemplate
    private RedisTemplate redisTemplate;

    public SpringTemplateRedisDao(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Set<String> keys(String pattern) {
        return redisTemplate.keys(pattern);
    }

    @Override
    public Object getObject(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    @Override
    public void deleteObject(String key) {
        redisTemplate.delete(key);
    }

    @Override
    public void saveObject(String key, Object value,long timeout) {
        redisTemplate.opsForValue().set(key, value, timeout, TimeUnit.MILLISECONDS);
    }

    @Override
    public void saveObject(String key, Object value) {
        redisTemplate.opsForValue().set(key,value);
    }

    @Override
    public long getExpire(String key) {
        return redisTemplate.getExpire(key,TimeUnit.MILLISECONDS);
    }
}
