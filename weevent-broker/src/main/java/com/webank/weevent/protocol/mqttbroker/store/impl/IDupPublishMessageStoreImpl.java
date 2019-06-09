package com.webank.weevent.protocol.mqttbroker.store.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Resource;

import com.webank.weevent.protocol.mqttbroker.store.dto.DupPublishMessageStore;
import com.webank.weevent.protocol.mqttbroker.store.IDupPublishMessageStore;
import com.webank.weevent.protocol.mqttbroker.store.IMessageIdStore;

import org.apache.ignite.IgniteCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author websterchen
 * @version v1.0
 * @since 2019/6/3
 */
@Service
public class IDupPublishMessageStoreImpl implements IDupPublishMessageStore {
    @Autowired
    private IMessageIdStore iMessageIdStore;
    @Resource
    private IgniteCache<String, ConcurrentHashMap<Integer, DupPublishMessageStore>> dupPublishMessageCache;

    @Override
    public void put(String clientId, DupPublishMessageStore dupPublishMessageStore) {
        ConcurrentHashMap<Integer, DupPublishMessageStore> map = dupPublishMessageCache.containsKey(clientId) ? dupPublishMessageCache.get(clientId) : new ConcurrentHashMap<Integer, DupPublishMessageStore>();
        map.put(dupPublishMessageStore.getMessageId(), dupPublishMessageStore);
        dupPublishMessageCache.put(clientId, map);
    }

    @Override
    public List<DupPublishMessageStore> get(String clientId) {
        if (dupPublishMessageCache.containsKey(clientId)) {
            ConcurrentHashMap<Integer, DupPublishMessageStore> map = dupPublishMessageCache.get(clientId);
            Collection<DupPublishMessageStore> collection = map.values();
            return new ArrayList<DupPublishMessageStore>(collection);
        }
        return new ArrayList<DupPublishMessageStore>();
    }

    @Override
    public void remove(String clientId, int messageId) {
        if (dupPublishMessageCache.containsKey(clientId)) {
            ConcurrentHashMap<Integer, DupPublishMessageStore> map = dupPublishMessageCache.get(clientId);
            if (map.containsKey(messageId)) {
                map.remove(messageId);
                if (map.size() > 0) {
                    dupPublishMessageCache.put(clientId, map);
                } else {
                    dupPublishMessageCache.remove(clientId);
                }
            }
        }
    }

    @Override
    public void removeByClient(String clientId) {
        if (dupPublishMessageCache.containsKey(clientId)) {
            ConcurrentHashMap<Integer, DupPublishMessageStore> map = dupPublishMessageCache.get(clientId);
            map.forEach((messageId, dupPublishMessageStore) -> {
                iMessageIdStore.releaseMessageId(messageId);
            });
            map.clear();
            dupPublishMessageCache.remove(clientId);
        }
    }
}
