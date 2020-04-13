package com.webank.weevent.client.jsonrpc;

import java.util.Map;

import com.webank.weevent.client.BaseResponse;
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
    BaseResponse<Boolean> open(String topic, String groupId) throws BrokerException;

    BaseResponse<Boolean> close(String topic, String groupId) throws BrokerException;

    BaseResponse<Boolean> exist(String topic, String groupId) throws BrokerException;

    BaseResponse<TopicPage> list(Integer pageIndex, Integer pageSize, String groupId) throws BrokerException;

    BaseResponse<TopicInfo> state(String topic, String groupId) throws BrokerException;

    BaseResponse<WeEvent> getEvent(String eventId, String groupId) throws BrokerException;
}
