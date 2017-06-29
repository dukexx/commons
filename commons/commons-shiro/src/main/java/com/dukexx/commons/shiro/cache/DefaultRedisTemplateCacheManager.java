package com.dukexx.commons.shiro.cache;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.shiro.cache.AbstractCacheManager;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * not achieve
 *
 * @author dukexx
 * @date 2017/4/14
 * @since 1.0.0
 */
@Deprecated
@NoArgsConstructor
@AllArgsConstructor
public class DefaultRedisTemplateCacheManager extends AbstractCacheManager {

    @Setter
    private RedisTemplate redisTemplate;

    @Override
    protected Cache createCache(String name) throws CacheException {
        return null;
    }

}
