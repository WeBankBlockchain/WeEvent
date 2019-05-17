package com.webank.weevent.protocol.jsonrpc;

import java.util.Map;

import com.webank.weevent.broker.fisco.util.WeEventUtils;
import com.webank.weevent.broker.ha.MasterJob;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.ErrorCode;
import com.webank.weevent.broker.plugin.IConsumer;
import com.webank.weevent.broker.plugin.IProducer;
import com.webank.weevent.sdk.SendResult;
import com.webank.weevent.sdk.TopicInfo;
import com.webank.weevent.sdk.TopicPage;
import com.webank.weevent.sdk.WeEvent;
import com.webank.weevent.sdk.jsonrpc.IBrokerRpc;

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
    private IConsumer consumer;

    private MasterJob masterJob;

    public BrokerRpc() {
    }

    @Autowired
    public void setProducer(IProducer producer) {
        this.producer = producer;
    }

    @Autowired
    public void setConsumer(IConsumer iConsumer) {
        this.consumer = iConsumer;
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

        return this.producer.publish(new WeEvent(topic, content, extensions), WeEventUtils.getGroupId(groupId));
    }

    @Override
    public WeEvent getEvent(@JsonRpcParam(value = "eventId") String eventId,
                            @JsonRpcParam(value = "groupId") String groupId) throws BrokerException {
        return this.producer.getEvent(eventId, WeEventUtils.getGroupId(groupId));
    }

    @Override
    public String subscribe(@JsonRpcParam(value = "topic") String topic,
                            @JsonRpcParam(value = "groupId") String groupId,
                            @JsonRpcParam(value = "subscriptionId") String subscriptionId,
                            @JsonRpcParam(value = "url") String url) throws BrokerException {
        checkSupport();
        return this.masterJob.getCgiSubscription().jsonRpcSubscribe(topic, WeEventUtils.getGroupId(groupId), subscriptionId, url);
    }

    @Override
    public boolean unSubscribe(@JsonRpcParam(value = "subscriptionId") String subscriptionId) throws BrokerException {
        checkSupport();
        return this.masterJob.getCgiSubscription().jsonRpcUnSubscribe(subscriptionId);
    }

    @Override
    public boolean open(@JsonRpcParam(value = "topic") String topic,
                        @JsonRpcParam(value = "groupId") String groupId) throws BrokerException {
        return this.producer.open(topic, WeEventUtils.getGroupId(groupId));
    }

    @Override
    public boolean close(@JsonRpcParam(value = "topic") String topic,
                         @JsonRpcParam(value = "groupId") String groupId) throws BrokerException {
        return this.producer.close(topic, WeEventUtils.getGroupId(groupId));
    }

    @Override
    public boolean exist(@JsonRpcParam(value = "topic") String topic,
                         @JsonRpcParam(value = "groupId") String groupId) throws BrokerException {
        return this.producer.exist(topic, WeEventUtils.getGroupId(groupId));
    }

    @Override
    public TopicPage list(@JsonRpcParam(value = "pageIndex") Integer pageIndex,
                          @JsonRpcParam(value = "pageSize") Integer pageSize,
                          @JsonRpcParam(value = "groupId") String groupId) throws BrokerException {
        return this.producer.list(pageIndex, pageSize, WeEventUtils.getGroupId(groupId));
    }

    @Override
    public TopicInfo state(@JsonRpcParam(value = "topic") String topic,
                           @JsonRpcParam(value = "groupId") String groupId) throws BrokerException {
        return this.producer.state(topic, WeEventUtils.getGroupId(groupId));
    }
}
