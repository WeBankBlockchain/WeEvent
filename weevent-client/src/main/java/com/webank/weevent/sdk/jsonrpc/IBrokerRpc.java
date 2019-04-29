package com.webank.weevent.sdk.jsonrpc;


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
    SendResult publish(@JsonRpcParam(value = "topic") String topic,
                       @JsonRpcParam(value = "content") byte[] content) throws BrokerException;

    // Interface for consumer.
    String subscribe(@JsonRpcParam(value = "topic") String topic,
                     @JsonRpcParam(value = "subscriptionId") String subscriptionId,
                     @JsonRpcParam(value = "url") String url) throws BrokerException;

    boolean unSubscribe(@JsonRpcParam(value = "subscriptionId") String subscriptionId) throws BrokerException;

    // The following is interface for IEventTopic.
    boolean open(@JsonRpcParam(value = "topic") String topic) throws BrokerException;

    boolean close(@JsonRpcParam(value = "topic") String topic) throws BrokerException;

    boolean exist(@JsonRpcParam(value = "topic") String topic) throws BrokerException;

    TopicPage list(@JsonRpcParam(value = "pageIndex") Integer pageIndex,
                   @JsonRpcParam(value = "pageSize") Integer pageSize) throws BrokerException;

    TopicInfo state(@JsonRpcParam(value = "topic") String topic) throws BrokerException;

    WeEvent getEvent(@JsonRpcParam(value = "eventId") String eventId) throws BrokerException;
}
