package com.webank.weevent.sdk.jsonrpc;

import java.util.Map;

import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.SendResult;
import com.webank.weevent.sdk.TopicInfo;
import com.webank.weevent.sdk.TopicPage;
import com.webank.weevent.sdk.WeEvent;

/**
 * Interface for rpc, inlcude RESTFul and JsonRPC.
 *
 * @author matthewliu
 * @since 2018/11/21
 */
public interface IBrokerRpc {
    // Interface for producer.
    default SendResult publish(String topic, byte[] content) throws BrokerException {
        return null;
    }

    default SendResult publish(String topic, String groupId, byte[] content, Map<String, String> extensions) throws BrokerException {
        return null;
    }

    // The following is interface for IEventTopic.
    boolean open(String topic, String groupId) throws BrokerException;

    default boolean open(String topic) throws BrokerException {
        return false;
    }

    boolean close(String topic, String groupId) throws BrokerException;

    default boolean close(String topic) throws BrokerException {
        return false;
    }

    boolean exist(String topic, String groupId) throws BrokerException;

    default boolean exist(String topic) throws BrokerException {
        return false;
    }

    TopicPage list(Integer pageIndex, Integer pageSize, String groupId) throws BrokerException;

    default TopicPage list(Integer pageIndex, Integer pageSize) throws BrokerException {
        return null;
    }

    TopicInfo state(String topic, String groupId) throws BrokerException;

    default TopicInfo state(String topic) throws BrokerException {
        return null;
    }

    WeEvent getEvent(String eventId, String groupId) throws BrokerException;

    default WeEvent getEvent(String eventId) throws BrokerException {
        return null;
    }
}
