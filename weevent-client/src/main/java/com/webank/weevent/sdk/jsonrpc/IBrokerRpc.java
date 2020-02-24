package com.webank.weevent.sdk.jsonrpc;

import java.util.Map;

import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.SendResult;
import com.webank.weevent.sdk.TopicInfo;
import com.webank.weevent.sdk.TopicPage;
import com.webank.weevent.sdk.WeEvent;

/**
 * Core interfaces for rpc, include RESTFul and JsonRPC.
 *
 * @author matthewliu
 * @since 2018/11/21
 */
public interface IBrokerRpc {
    // Interface for producer.
    default SendResult publish(String topic, String groupId, byte[] content, Map<String, String> extensions) throws BrokerException {
        return null;
    }

    // The following is interface for IEventTopic.
    boolean open(String topic, String groupId) throws BrokerException;

    boolean close(String topic, String groupId) throws BrokerException;

    boolean exist(String topic, String groupId) throws BrokerException;

    TopicPage list(Integer pageIndex, Integer pageSize, String groupId) throws BrokerException;

    TopicInfo state(String topic, String groupId) throws BrokerException;

    WeEvent getEvent(String eventId, String groupId) throws BrokerException;
}
