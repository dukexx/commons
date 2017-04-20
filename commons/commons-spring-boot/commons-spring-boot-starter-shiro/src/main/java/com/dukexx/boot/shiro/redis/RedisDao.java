package com.dukexx.boot.shiro.redis;

import java.util.Set;

/**
 * Created by dukexx on 2017/4/10.
 */
public interface RedisDao<T> {

    /**
     * 根据pattern获取key集合
     * @param pattern
     * @return
     */
    Set<String> keys(String pattern);

    /**
     * 根据key获取值
     * @param key
     * @return
     */
    T getObject(String key);

    /**
     * 根据key删除数据
     * @param key
     */
    void deleteObject(String key);

    /**
     * 保存key-value
     * @param key
     * @param value
     */
    void saveObject(String key, T value,long timeout);

    /**
     * 保存key-value
     * @param key
     * @param value
     */
    void saveObject(String key, T value);

    /**
     * getExpire
     * @param key
     * @return
     */
    long getExpire(String key);
}
