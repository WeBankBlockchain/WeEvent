package com.webank.weevent.broker.protocol.mqtt.store;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.extern.slf4j.Slf4j;

/**
 * @author websterchen
 * @version v1.0
 * @since 2019/6/8
 */
@Slf4j
public class MessageIdStore {
    private final int MIN_MSG_ID = 1;
    private final int MAX_MSG_ID = 65535;
    private Map<Integer, Integer> messageIdCache = new ConcurrentHashMap<>();
    private int nextMsgId = MIN_MSG_ID - 1;

    public int getNextMessageId() {
        do {
            nextMsgId++;
            if (nextMsgId > MAX_MSG_ID) {
                nextMsgId = MIN_MSG_ID;
            }
        } while (messageIdCache.containsKey(nextMsgId));
        messageIdCache.put(nextMsgId, nextMsgId);
        return nextMsgId;
    }

    public void releaseMessageId(int messageId) {
        try {
            messageIdCache.remove(messageId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
