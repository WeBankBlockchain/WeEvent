package com.webank.weevent.broker.protocol.mqtt.store.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.webank.weevent.broker.protocol.mqtt.store.ISubscribeStore;
import com.webank.weevent.broker.protocol.mqtt.store.dto.SubscribeStore;

/**
 * @author websterchen
 * @version v1.0
 * @since 2019/6/3
 */
public class ISubscribeStoreImpl implements ISubscribeStore {
    private Map<String, ConcurrentHashMap<String, SubscribeStore>> subscribeCache = new ConcurrentHashMap<>();

    @Override
    public void put(String topicFilter, SubscribeStore subscribeStore) {
        ConcurrentHashMap<String, SubscribeStore> map =
                subscribeCache.containsKey(topicFilter) ? subscribeCache.get(topicFilter) : new ConcurrentHashMap<String, SubscribeStore>();
        map.put(subscribeStore.getClientId(), subscribeStore);
        subscribeCache.put(topicFilter, map);
    }

    @Override
    public SubscribeStore get(String topicFilter, String clientId) {
        if (0 != subscribeCache.size() && !subscribeCache.get(topicFilter).isEmpty()) {
            return subscribeCache.get(topicFilter).get(clientId);
        }
        return null;
    }

    @Override
    public void remove(String topicFilter, String clientId) {
        if (subscribeCache.containsKey(topicFilter)) {
            ConcurrentHashMap<String, SubscribeStore> map = subscribeCache.get(topicFilter);
            if (map.containsKey(clientId)) {
                map.remove(clientId);
                if (map.size() > 0) {
                    subscribeCache.put(topicFilter, map);
                } else {
                    subscribeCache.remove(topicFilter);
                }
            }
        }
    }

    @Override
    public void removeForClient(String clientId) {
        for (Map.Entry<String, ConcurrentHashMap<String, SubscribeStore>> entry : subscribeCache.entrySet()) {
            ConcurrentHashMap<String, SubscribeStore> map = entry.getValue();
            if (map.containsKey(clientId)) {
                map.remove(clientId);
                if (map.size() > 0) {
                    subscribeCache.put(entry.getKey(), map);
                } else {
                    subscribeCache.remove(entry.getKey());
                }
            }
        }
    }

    @Override
    public List<SubscribeStore> searchByTopic(String topic) {
        List<SubscribeStore> subscribeStores = new ArrayList<SubscribeStore>();
        if (subscribeCache.containsKey(topic)) {
            ConcurrentHashMap<String, SubscribeStore> map = subscribeCache.get(topic);
            Collection<SubscribeStore> collection = map.values();
            List<SubscribeStore> list = new ArrayList<SubscribeStore>(collection);
            subscribeStores.addAll(list);
        }
        return subscribeStores;
    }

    @Override
    public List<SubscribeStore> searchByClientId(String clientId) {
        List<SubscribeStore> subscribeStores = new ArrayList<SubscribeStore>();
        for (Map.Entry<String, ConcurrentHashMap<String, SubscribeStore>> entry : subscribeCache.entrySet()) {
            ConcurrentHashMap<String, SubscribeStore> map = entry.getValue();
            if (map.containsKey(clientId)) {
                subscribeStores.add(map.get(clientId));
            }
        }
        return subscribeStores;
    }
}
