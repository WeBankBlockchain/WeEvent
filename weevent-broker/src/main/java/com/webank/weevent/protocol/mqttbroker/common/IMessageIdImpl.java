package com.webank.weevent.protocol.mqttbroker.common;

import java.util.Map;
import java.util.concurrent.locks.Lock;

import javax.annotation.Resource;

import org.apache.ignite.IgniteCache;
import org.springframework.stereotype.Service;

/**
 *@ClassName IMessageIdImpl
 *@Description TODO
 *@Author websterchen
 *@Date 2019/5/22 16:20
 *@Version 1.0
 **/
@Service
public class IMessageIdImpl implements IMessageId {
    private final int MIN_MSG_ID = 1;

    private final int MAX_MSG_ID = 65535;

    private final int lock = 0;

    private Map<Integer, Integer> messageIdCache;

    private int nextMsgId = MIN_MSG_ID - 1;

    @Override
    public int getNextMessageId() {
        //Lock lock = messageIdCache.lock(this.lock);
        Lock lock = null;
        lock.lock();
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
        } finally {
            lock.unlock();
        }
        return nextMsgId;
    }

    @Override
    public void releaseMessageId(int messageId) {
        //Lock lock = messageIdCache.lock(this.lock);
        Lock lock = null;
        lock.lock();
        try {
            messageIdCache.remove(messageId);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }
}
