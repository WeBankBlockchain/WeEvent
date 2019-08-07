package com.webank.weevent.broker.fisco.util;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * FIFO queue, base on LinkedHashMap
 *
 * @author matthewliu
 * @since 2018/08/07
 */
public class FIFOCache<K, V> extends LinkedHashMap<K, V> {
    private Integer capacity;

    public FIFOCache(Integer capacity) {
        super(capacity, 0.75f, false);
        this.capacity = capacity;
    }

    @Override
    public boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > capacity;
    }
}
