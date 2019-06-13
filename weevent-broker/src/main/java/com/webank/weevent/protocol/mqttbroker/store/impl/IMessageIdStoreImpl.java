package com.webank.weevent.protocol.mqttbroker.store.impl;

import org.apache.ignite.IgniteCache;
import org.apache.ignite.internal.processors.cache.IgniteCacheProxyImpl;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;

import javax.annotation.Resource;

import com.webank.weevent.protocol.mqttbroker.store.IMessageIdStore;

/**
 * @author websterchen
 * @version v1.0
 * @since 2019/6/8
 */
@Service
public class IMessageIdStoreImpl implements IMessageIdStore {
    private final int MIN_MSG_ID = 1;
    private final int MAX_MSG_ID = 65535;
    private Map<Integer, Integer> messageIdCache = new ConcurrentHashMap<>();
    private int nextMsgId = MIN_MSG_ID - 1;

    @Override
    public int getNextMessageId() {
        try {
            do {
                nextMsgId++;
                if (nextMsgId > MAX_MSG_ID) {
                    nextMsgId = MIN_MSG_ID;
                }
            } while (messageIdCache.containsKey(nextMsgId));
            messageIdCache.put(nextMsgId, nextMsgId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return nextMsgId;
    }

    @Override
    public void releaseMessageId(int messageId) {
        try {
            messageIdCache.remove(messageId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
