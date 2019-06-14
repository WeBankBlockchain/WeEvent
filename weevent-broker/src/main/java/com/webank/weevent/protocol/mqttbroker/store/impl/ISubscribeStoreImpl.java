package com.webank.weevent.protocol.mqttbroker.store.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.webank.weevent.protocol.mqttbroker.store.ISubscribeStore;
import com.webank.weevent.protocol.mqttbroker.store.dto.SubscribeStore;

import cn.hutool.core.util.StrUtil;
import org.springframework.stereotype.Service;

/**
 * @author websterchen
 * @version v1.0
 * @since 2019/6/3
 */
@Service
public class ISubscribeStoreImpl implements ISubscribeStore {
    private Map<String, ConcurrentHashMap<String, SubscribeStore>> subscribeNotWildcardCache = new ConcurrentHashMap<>();
    private Map<String, ConcurrentHashMap<String, SubscribeStore>> subscribeWildcardCache = new ConcurrentHashMap<>();

    @Override
    public void put(String topicFilter, SubscribeStore subscribeStore) {
        if (StrUtil.contains(topicFilter, '#') || StrUtil.contains(topicFilter, '+')) {
            ConcurrentHashMap<String, SubscribeStore> map =
                    subscribeWildcardCache.containsKey(topicFilter) ? subscribeWildcardCache.get(topicFilter) : new ConcurrentHashMap<String, SubscribeStore>();
            map.put(subscribeStore.getClientId(), subscribeStore);
            subscribeWildcardCache.put(topicFilter, map);
        } else {
            ConcurrentHashMap<String, SubscribeStore> map =
                    subscribeNotWildcardCache.containsKey(topicFilter) ? subscribeNotWildcardCache.get(topicFilter) : new ConcurrentHashMap<String, SubscribeStore>();
            map.put(subscribeStore.getClientId(), subscribeStore);
            subscribeNotWildcardCache.put(topicFilter, map);
        }
    }

    @Override
    public SubscribeStore get(String topicFilter, String clientId) {
        if (subscribeNotWildcardCache.get(topicFilter).get(clientId).getClientId().isEmpty()) {
            return subscribeWildcardCache.get(topicFilter).get(clientId);
        }
        return subscribeNotWildcardCache.get(topicFilter).get(clientId);
    }

    @Override
    public void remove(String topicFilter, String clientId) {
        if (StrUtil.contains(topicFilter, '#') || StrUtil.contains(topicFilter, '+')) {
            if (subscribeWildcardCache.containsKey(topicFilter)) {
                ConcurrentHashMap<String, SubscribeStore> map = subscribeWildcardCache.get(topicFilter);
                if (map.containsKey(clientId)) {
                    map.remove(clientId);
                    if (map.size() > 0) {
                        subscribeWildcardCache.put(topicFilter, map);
                    } else {
                        subscribeWildcardCache.remove(topicFilter);
                    }
                }
            }
        } else {
            if (subscribeNotWildcardCache.containsKey(topicFilter)) {
                ConcurrentHashMap<String, SubscribeStore> map = subscribeNotWildcardCache.get(topicFilter);
                if (map.containsKey(clientId)) {
                    map.remove(clientId);
                    if (map.size() > 0) {
                        subscribeNotWildcardCache.put(topicFilter, map);
                    } else {
                        subscribeNotWildcardCache.remove(topicFilter);
                    }
                }
            }
        }
    }

    @Override
    public void removeForClient(String clientId) {
        for (Map.Entry<String, ConcurrentHashMap<String, SubscribeStore>> entry : subscribeNotWildcardCache.entrySet()) {
            ConcurrentHashMap<String, SubscribeStore> map = entry.getValue();
            if (map.containsKey(clientId)) {
                map.remove(clientId);
                if (map.size() > 0) {
                    subscribeNotWildcardCache.put(entry.getKey(), map);
                } else {
                    subscribeNotWildcardCache.remove(entry.getKey());
                }
            }
        }

        for (Map.Entry<String, ConcurrentHashMap<String, SubscribeStore>> entry : subscribeWildcardCache.entrySet()) {
            ConcurrentHashMap<String, SubscribeStore> map = entry.getValue();
            if (map.containsKey(clientId)) {
                map.remove(clientId);
                if (map.size() > 0) {
                    subscribeWildcardCache.put(entry.getKey(), map);
                } else {
                    subscribeWildcardCache.remove(entry.getKey());
                }
            }
        }
    }

    @Override
    public List<SubscribeStore> search(String topic) {
        List<SubscribeStore> subscribeStores = new ArrayList<SubscribeStore>();
        if (subscribeNotWildcardCache.containsKey(topic)) {
            ConcurrentHashMap<String, SubscribeStore> map = subscribeNotWildcardCache.get(topic);
            Collection<SubscribeStore> collection = map.values();
            List<SubscribeStore> list = new ArrayList<SubscribeStore>(collection);
            subscribeStores.addAll(list);
        }

        for (Map.Entry<String, ConcurrentHashMap<String, SubscribeStore>> entry : subscribeWildcardCache.entrySet()) {
            String topicFilter = entry.getKey();
            if (StrUtil.split(topic, '/').size() >= StrUtil.split(topicFilter, '/').size()) {
                List<String> splitTopics = StrUtil.split(topic, '/');
                List<String> spliteTopicFilters = StrUtil.split(topicFilter, '/');
                String newTopicFilter = "";
                for (int i = 0; i < spliteTopicFilters.size(); i++) {
                    String value = spliteTopicFilters.get(i);
                    if (value.equals("+")) {
                        newTopicFilter = newTopicFilter + "+/";
                    } else if (value.equals("#")) {
                        newTopicFilter = newTopicFilter + "#/";
                        break;
                    } else {
                        newTopicFilter = newTopicFilter + splitTopics.get(i) + "/";
                    }
                }
                newTopicFilter = StrUtil.removeSuffix(newTopicFilter, "/");
                if (topicFilter.equals(newTopicFilter)) {
                    ConcurrentHashMap<String, SubscribeStore> map = entry.getValue();
                    Collection<SubscribeStore> collection = map.values();
                    List<SubscribeStore> list = new ArrayList<SubscribeStore>(collection);
                    subscribeStores.addAll(list);
                }
            }
        }
        return subscribeStores;
    }
}
