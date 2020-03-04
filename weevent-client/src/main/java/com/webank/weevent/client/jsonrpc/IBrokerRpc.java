package com.webank.weevent.client.jsonrpc;

import java.util.Map;

import com.webank.weevent.client.BrokerException;
import com.webank.weevent.client.SendResult;
import com.webank.weevent.client.TopicInfo;
import com.webank.weevent.client.TopicPage;
import com.webank.weevent.client.WeEvent;

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
