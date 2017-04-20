package com.dukexx.boot.shiro.commons.collection;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author dukexx
 * @date 2017/4/14
 * @since 1.0.0
 */
public class ReadWriteMap<K,V> implements Map<K,V>,Serializable {

    private static final long serialVersionUID = 1L;

    private ReentrantReadWriteLock lock;

    private ReentrantReadWriteLock.ReadLock readLock;

    private ReentrantReadWriteLock.WriteLock writeLock;

    private Map<K,V> map;

    public ReadWriteMap(Map<K, V> map) {
        this(map, false);
    }

    public ReadWriteMap(Map<K, V> map, boolean fair) {
        this(map, new ReentrantReadWriteLock(fair));
    }

    public ReadWriteMap(Map<K, V> map, ReentrantReadWriteLock lock) {
        this.map=map;
        this.lock = lock;
        readLock = lock.readLock();
        writeLock = lock.writeLock();
    }

    @Override
    public int size() {
        readLock.lock();
        try {
            return map.size();
        }finally {
            readLock.unlock();
        }
    }

    @Override
    public boolean isEmpty() {
        readLock.lock();
        try {
            return map.isEmpty();
        }finally {
            readLock.unlock();
        }
    }

    @Override
    public boolean containsKey(Object key) {
        readLock.lock();
        try {
            return map.containsKey(key);
        }finally {
            readLock.unlock();
        }
    }

    @Override
    public boolean containsValue(Object value) {
        readLock.lock();
        try {
            return map.containsValue(value);
        }finally {
            readLock.unlock();
        }
    }

    @Override
    public V get(Object key) {
        readLock.lock();
        try {
            return map.get(key);
        }finally {
            readLock.unlock();
        }
    }

    @Override
    public V put(K key, V value) {
        writeLock.lock();
        try {
            return map.put(key,value);
        }finally {
            writeLock.unlock();
        }
    }

    @Override
    public V remove(Object key) {
        writeLock.lock();
        try {
            return map.remove(key);
        }finally {
            writeLock.unlock();
        }
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        writeLock.lock();
        try {
            map.putAll(m);
        }finally {
            writeLock.unlock();
        }
    }

    @Override
    public void clear() {
        writeLock.lock();
        try {
            map.clear();
        }finally {
            writeLock.unlock();
        }
    }

    @Override
    public Set<K> keySet() {
        readLock.lock();
        try {
            return map.keySet();
        }finally {
            readLock.unlock();
        }
    }

    @Override
    public Collection<V> values() {
        readLock.lock();
        try {
            return map.values();
        }finally {
            readLock.unlock();
        }
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        readLock.lock();
        try {
            return map.entrySet();
        }finally {
            readLock.unlock();
        }
    }

    public ReentrantReadWriteLock.ReadLock getReadLock() {
        return this.readLock;
    }

    public ReentrantReadWriteLock.WriteLock getWriteLock() {
        return this.writeLock;
    }

}
