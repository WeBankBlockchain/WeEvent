package com.webank.weevent.protocol.mqttbroker.store.impl;

import org.apache.ignite.IgniteCache;
import org.apache.ignite.internal.processors.cache.IgniteCacheProxyImpl;
import org.springframework.stereotype.Service;

import java.util.concurrent.locks.Lock;

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

    private final int lock = 0;
    private IgniteCache<Integer, Integer> messageIdMap = new IgniteCacheProxyImpl<>();
    private int nextMsgId = MIN_MSG_ID - 1;

    @Override
    public int getNextMessageId() {
        Lock lock = messageIdMap.lock(this.lock);
        lock.lock();
        try {
            do {
                nextMsgId++;
                if (nextMsgId > MAX_MSG_ID) {
                    nextMsgId = MIN_MSG_ID;
                }
            } while (messageIdMap.containsKey(nextMsgId));
            messageIdMap.put(nextMsgId, nextMsgId);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        return nextMsgId;
    }

    @Override
    public void releaseMessageId(int messageId) {
        Lock lock = messageIdMap.lock(this.lock);
        lock.lock();
        try {
            messageIdMap.remove(messageId);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }
}
