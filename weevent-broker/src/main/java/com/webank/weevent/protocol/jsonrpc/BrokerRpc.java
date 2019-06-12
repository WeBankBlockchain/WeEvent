package com.webank.weevent.protocol.jsonrpc;

import java.util.HashMap;
import java.util.Map;

import com.webank.weevent.broker.fisco.constant.WeEventConstants;
import com.webank.weevent.broker.ha.MasterJob;
import com.webank.weevent.broker.plugin.IProducer;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.ErrorCode;
import com.webank.weevent.sdk.SendResult;
import com.webank.weevent.sdk.TopicInfo;
import com.webank.weevent.sdk.TopicPage;
import com.webank.weevent.sdk.WeEvent;
import com.webank.weevent.sdk.jsonrpc.IBrokerRpc;

import com.alibaba.fastjson.annotation.JSONType;
import com.googlecode.jsonrpc4j.JsonRpcParam;
import com.googlecode.jsonrpc4j.spring.AutoJsonRpcServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Implement of JsonRpc service.
 *
 * @author matthewliu
 * @since 2018/11/21
 */
@Slf4j
@AutoJsonRpcServiceImpl
@Component
public class BrokerRpc implements IBrokerRpc {
    private IProducer producer;

    private MasterJob masterJob;

    public BrokerRpc() {
    }

    @Autowired
    public void setProducer(IProducer producer) {
        this.producer = producer;
    }

    @Autowired(required = false)
    public void setMasterJob(MasterJob masterJob) {
        this.masterJob = masterJob;
    }

    private void checkSupport() throws BrokerException {
        if (this.masterJob == null) {
            log.error("no broker.zookeeper.ip configuration, skip it");
            throw new BrokerException(ErrorCode.CGI_SUBSCRIPTION_NO_ZOOKEEPER);
        }
    }

    @Override
    public SendResult publish(@JsonRpcParam(value = "topic") String topic,
                              @JsonRpcParam(value = "groupId") String groupId,
                              @JsonRpcParam(value = "content") byte[] content,
                              @JsonRpcParam(value = "extensions") Map<String, String> extensions) throws BrokerException {

        return this.producer.publish(new WeEvent(topic, content, extensions), groupId);
    }

    @Override
    public SendResult publish(@JsonRpcParam(value = "topic") String topic,
                              @JsonRpcParam(value = "content") byte[] content,
                              @JsonRpcParam(value = "extensions") Map<String, String> extensions) throws BrokerException {

        return this.producer.publish(new WeEvent(topic, content, extensions), WeEventConstants.DEFAULT_GROUP_ID);
    }

    @Override
    public SendResult publish(@JsonRpcParam(value = "topic") String topic,
                              @JsonRpcParam(value = "content") byte[] content) throws BrokerException {

        return this.producer.publish(new WeEvent(topic, content, new HashMap<>()), WeEventConstants.DEFAULT_GROUP_ID);
    }

    @Override
    public SendResult publish(@JsonRpcParam(value = "topic") String topic,
                              @JsonRpcParam(value = "groupId") String groupId,
                              @JsonRpcParam(value = "content") byte[] content) throws BrokerException {

        return this.producer.publish(new WeEvent(topic, content, new HashMap<>()), groupId);
    }

    @Override
    public WeEvent getEvent(@JsonRpcParam(value = "eventId") String eventId,
                            @JsonRpcParam(value = "groupId") String groupId) throws BrokerException {
        return this.producer.getEvent(eventId, groupId);
    }

    @Override
    public WeEvent getEvent(@JsonRpcParam(value = "eventId") String eventId) throws BrokerException {
        return this.producer.getEvent(eventId, WeEventConstants.DEFAULT_GROUP_ID);
    }

    @Override
    public String subscribe(@JsonRpcParam(value = "topic") String topic,
                            @JsonRpcParam(value = "groupId") String groupId,
                            @JsonRpcParam(value = "subscriptionId") String subscriptionId,
                            @JsonRpcParam(value = "url") String url) throws BrokerException {
        return this.masterJob.doSubscribe(WeEventConstants.JSONRPCTYPE, topic, groupId, subscriptionId, url, "");
    }

    @Override
    public String subscribe(@JsonRpcParam(value = "topic") String topic,
                            @JsonRpcParam(value = "subscriptionId") String subscriptionId,
                            @JsonRpcParam(value = "url") String url) throws BrokerException {

        return this.masterJob.doSubscribe(WeEventConstants.JSONRPCTYPE, topic, WeEventConstants.DEFAULT_GROUP_ID, subscriptionId, url, "");
    }

    @Override
    public String subscribe(@JsonRpcParam(value = "topic") String topic,
                            @JsonRpcParam(value = "url") String url) throws BrokerException {
        return this.masterJob.doSubscribe(WeEventConstants.JSONRPCTYPE, topic, WeEventConstants.DEFAULT_GROUP_ID, "", url, "");
    }

    @Override
    public boolean unSubscribe(@JsonRpcParam(value = "subscriptionId") String subscriptionId) throws BrokerException {
        return this.masterJob.doUnsubscribe(WeEventConstants.JSONRPCTYPE, subscriptionId, "");
    }

    @Override
    public boolean open(@JsonRpcParam(value = "topic") String topic,
                        @JsonRpcParam(value = "groupId") String groupId) throws BrokerException {
        return this.producer.open(topic, groupId);
    }

    @Override
    public boolean open(@JsonRpcParam(value = "topic") String topic) throws BrokerException {
        return this.producer.open(topic, WeEventConstants.DEFAULT_GROUP_ID);
    }

    @Override
    public boolean close(@JsonRpcParam(value = "topic") String topic,
                         @JsonRpcParam(value = "groupId") String groupId) throws BrokerException {
        return this.producer.close(topic, groupId);
    }

    @Override
    public boolean close(@JsonRpcParam(value = "topic") String topic) throws BrokerException {
        return this.producer.close(topic, WeEventConstants.DEFAULT_GROUP_ID);
    }

    @Override
    public boolean exist(@JsonRpcParam(value = "topic") String topic,
                         @JsonRpcParam(value = "groupId") String groupId) throws BrokerException {
        return this.producer.exist(topic, groupId);
    }

    @Override
    public boolean exist(@JsonRpcParam(value = "topic") String topic) throws BrokerException {
        return this.producer.exist(topic, WeEventConstants.DEFAULT_GROUP_ID);
    }

    @Override
    public TopicPage list(@JsonRpcParam(value = "pageIndex") Integer pageIndex,
                          @JsonRpcParam(value = "pageSize") Integer pageSize,
                          @JsonRpcParam(value = "groupId") String groupId) throws BrokerException {
        return this.producer.list(pageIndex, pageSize, groupId);
    }

    @Override
    public TopicPage list(@JsonRpcParam(value = "pageIndex") Integer pageIndex,
                          @JsonRpcParam(value = "pageSize") Integer pageSize) throws BrokerException {
        return this.producer.list(pageIndex, pageSize, WeEventConstants.DEFAULT_GROUP_ID);
    }

    @Override
    public TopicInfo state(@JsonRpcParam(value = "topic") String topic,
                           @JsonRpcParam(value = "groupId") String groupId) throws BrokerException {
        return this.producer.state(topic, groupId);
    }

    @Override
    public TopicInfo state(@JsonRpcParam(value = "topic") String topic) throws BrokerException {
        return this.producer.state(topic, WeEventConstants.DEFAULT_GROUP_ID);
    }
}
