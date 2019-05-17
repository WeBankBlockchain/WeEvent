package com.webank.weevent.sdk.jsonrpc;


import java.util.Map;

import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.SendResult;
import com.webank.weevent.sdk.TopicInfo;
import com.webank.weevent.sdk.TopicPage;
import com.webank.weevent.sdk.WeEvent;

import com.googlecode.jsonrpc4j.JsonRpcParam;
import com.googlecode.jsonrpc4j.JsonRpcService;

/**
 * Interface for RpcJson.
 * It's different from java interface, do not extends directly.
 *
 * @author matthewliu
 * @since 2018/11/21
 */
@JsonRpcService("/jsonrpc")
public interface IBrokerRpc {
    // Interface for producer.
    default SendResult publish(@JsonRpcParam(value = "topic") String topic,
                               @JsonRpcParam(value = "content") byte[] content,
                               @JsonRpcParam(value = "extensions") Map<String, String> extensions) throws BrokerException {
        return null;
    }

    default SendResult publish(@JsonRpcParam(value = "extensions") Map<String, String> extensions) throws BrokerException {
        return null;
    }

    // Interface for consumer.
    String subscribe(@JsonRpcParam(value = "topic") String topic,
                     @JsonRpcParam(value = "groupId") String groupId,
                     @JsonRpcParam(value = "subscriptionId") String subscriptionId,
                     @JsonRpcParam(value = "url") String url) throws BrokerException;

    boolean unSubscribe(@JsonRpcParam(value = "subscriptionId") String subscriptionId) throws BrokerException;

    // The following is interface for IEventTopic.
    boolean open(@JsonRpcParam(value = "topic") String topic,
                 @JsonRpcParam(value = "groupId") String groupId) throws BrokerException;

    boolean close(@JsonRpcParam(value = "topic") String topic,
                  @JsonRpcParam(value = "groupId") String groupId) throws BrokerException;

    boolean exist(@JsonRpcParam(value = "topic") String topic,
                  @JsonRpcParam(value = "groupId") String groupId) throws BrokerException;

    TopicPage list(@JsonRpcParam(value = "pageIndex") Integer pageIndex,
                   @JsonRpcParam(value = "pageSize") Integer pageSize,
                   @JsonRpcParam(value = "groupId") String groupId) throws BrokerException;

    TopicInfo state(@JsonRpcParam(value = "topic") String topic,
                    @JsonRpcParam(value = "groupId") String groupId) throws BrokerException;

    WeEvent getEvent(@JsonRpcParam(value = "eventId") String eventId,
                     @JsonRpcParam(value = "groupId") String groupId) throws BrokerException;
}
