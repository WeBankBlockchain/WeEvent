package com.webank.weevent.broker.fisco.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * L2 cache based on Redis
 * @author v_wbhwliu
 *
 */
public class LRUCache<K, V> extends LinkedHashMap<K, V> {

    private static final long serialVersionUID = -6798470043940063152L;	
    private Integer capacity;
    private final ReentrantLock lock = new ReentrantLock();
    
    public LRUCache(Integer capacity) {
        super(capacity, 0.75f, true);
        this.capacity = capacity;
    }
    
    @Override
    public V putIfAbsent(K key, V value) {
        try {
            this.lock.lock();
		    return super.putIfAbsent(key, value);
        } finally {
            this.lock.unlock();
        }
    }
    
    @Override
    public V get(Object key) {
        try {
            this.lock.lock();
            return super.get(key);
        } finally {
            this.lock.unlock();
        }
    }
   
    @Override
    public boolean removeEldestEntry(Map.Entry<K,V> eldest) {
        if(size() > capacity) {
            return true;
        }
        return false;
    }
}
