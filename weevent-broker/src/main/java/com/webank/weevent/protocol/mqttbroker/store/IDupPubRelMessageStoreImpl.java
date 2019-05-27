package com.webank.weevent.protocol.mqttbroker.store;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Resource;

import com.webank.weevent.protocol.mqttbroker.common.IMessageId;

import org.apache.ignite.IgniteCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *@ClassName IDupPubRelMessageStoreImpl
 *@Description TODO
 *@Author websterchen
 *@Date 2019/5/22 16:53
 *@Version 1.0
 **/
@Service
public class IDupPubRelMessageStoreImpl implements IDupPubRelMessageStore {
    @Autowired
    private IMessageId iMessageId;

    private Map<String, ConcurrentHashMap<Integer, DupPubRelMessageStore>> dupPubRelMessageCache = new ConcurrentHashMap();

    @Override
    public void put(String clientId, DupPubRelMessageStore dupPubRelMessageStore) {
        ConcurrentHashMap<Integer, DupPubRelMessageStore> map = dupPubRelMessageCache.containsKey(clientId) ? dupPubRelMessageCache.get(clientId) : new ConcurrentHashMap<Integer, DupPubRelMessageStore>();
        map.put(dupPubRelMessageStore.getMessageId(), dupPubRelMessageStore);
        dupPubRelMessageCache.put(clientId, map);
    }

    @Override
    public List<DupPubRelMessageStore> get(String clientId) {
        if (dupPubRelMessageCache.containsKey(clientId)) {
            ConcurrentHashMap<Integer, DupPubRelMessageStore> map = dupPubRelMessageCache.get(clientId);
            Collection<DupPubRelMessageStore> collection = map.values();
            return new ArrayList<DupPubRelMessageStore>(collection);
        }
        return new ArrayList<DupPubRelMessageStore>();
    }

    @Override
    public void remove(String clientId, int messageId) {
        if (dupPubRelMessageCache.containsKey(clientId)) {
            ConcurrentHashMap<Integer, DupPubRelMessageStore> map = dupPubRelMessageCache.get(clientId);
            if (map.containsKey(messageId)) {
                map.remove(messageId);
                if (map.size() > 0) {
                    dupPubRelMessageCache.put(clientId, map);
                } else {
                    dupPubRelMessageCache.remove(clientId);
                }
            }
        }
    }

    @Override
    public void removeByClient(String clientId) {
        if (dupPubRelMessageCache.containsKey(clientId)) {
            ConcurrentHashMap<Integer, DupPubRelMessageStore> map = dupPubRelMessageCache.get(clientId);
            map.forEach((messageId, dupPubRelMessageStore) -> {
                iMessageId.releaseMessageId(messageId);
            });
            map.clear();
            dupPubRelMessageCache.remove(clientId);
        }
    }
}
