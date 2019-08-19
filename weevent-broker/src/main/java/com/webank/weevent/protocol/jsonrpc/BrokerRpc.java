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

import com.alibaba.fastjson.JSON;
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
        log.info("jsonrpc protocol publish interface topic:{} groupId:{} contentLength:{} extensions:{}", topic, groupId, content.length, JSON.toJSONString(extensions));
        return this.producer.publish(new WeEvent(topic, content, extensions), groupId);
    }

    @Override
    public SendResult publish(@JsonRpcParam(value = "topic") String topic,
                              @JsonRpcParam(value = "content") byte[] content,
                              @JsonRpcParam(value = "extensions") Map<String, String> extensions) throws BrokerException {
        log.info("jsonrpc protocol publish interface topic:{} contentLength:{} extensions:{}", topic, content.length, JSON.toJSONString(extensions));
        return this.producer.publish(new WeEvent(topic, content, extensions), WeEvent.DEFAULT_GROUP_ID);
    }

    @Override
    public SendResult publish(@JsonRpcParam(value = "topic") String topic,
                              @JsonRpcParam(value = "content") byte[] content) throws BrokerException {
        log.info("jsonrpc protocol publish interface topic:{} contentLength:{}", topic, content.length);
        return this.producer.publish(new WeEvent(topic, content, new HashMap<>()), WeEvent.DEFAULT_GROUP_ID);
    }

    @Override
    public SendResult publish(@JsonRpcParam(value = "topic") String topic,
                              @JsonRpcParam(value = "groupId") String groupId,
                              @JsonRpcParam(value = "content") byte[] content) throws BrokerException {
        log.info("jsonrpc protocol publish interface topic:{} groupId:{} contentLength:{}", topic, groupId, content.length);
        return this.producer.publish(new WeEvent(topic, content, new HashMap<>()), groupId);
    }

    @Override
    public WeEvent getEvent(@JsonRpcParam(value = "eventId") String eventId,
                            @JsonRpcParam(value = "groupId") String groupId) throws BrokerException {
        log.info("jsonrpc protocol getEvent interface eventId:{} groupId:{}", eventId, groupId);
        return this.producer.getEvent(eventId, groupId);
    }

    @Override
    public WeEvent getEvent(@JsonRpcParam(value = "eventId") String eventId) throws BrokerException {
        log.info("jsonrpc protocol getEvent interface eventId:{}", eventId);
        return this.producer.getEvent(eventId, WeEvent.DEFAULT_GROUP_ID);
    }

    @Override
    public String subscribe(@JsonRpcParam(value = "topic") String topic,
                            @JsonRpcParam(value = "groupId") String groupId,
                            @JsonRpcParam(value = "subscriptionId") String subscriptionId,
                            @JsonRpcParam(value = "url") String url) throws BrokerException {
        log.info("jsonrpc protocol subscribe interface topic:{} groupId:{} subscriptionId:{} url:{}", topic, groupId, subscriptionId, url);
        return this.masterJob.doSubscribe(WeEventConstants.JSONRPCTYPE, topic, groupId, subscriptionId, url, "");
    }

    @Override
    public String subscribe(@JsonRpcParam(value = "topic") String topic,
                            @JsonRpcParam(value = "subscriptionId") String subscriptionId,
                            @JsonRpcParam(value = "url") String url) throws BrokerException {
        log.info("jsonrpc protocol subscribe interface topic:{} subscriptionId:{} url:{}", topic, subscriptionId, url);
        return this.masterJob.doSubscribe(WeEventConstants.JSONRPCTYPE, topic, WeEvent.DEFAULT_GROUP_ID, subscriptionId, url, "");
    }

    @Override
    public String subscribe(@JsonRpcParam(value = "topic") String topic,
                            @JsonRpcParam(value = "url") String url) throws BrokerException {
        log.info("jsonrpc protocol subscribe interface topic:{} url:{}", topic, url);
        return this.masterJob.doSubscribe(WeEventConstants.JSONRPCTYPE, topic, WeEvent.DEFAULT_GROUP_ID, "", url, "");
    }

    @Override
    public boolean unSubscribe(@JsonRpcParam(value = "subscriptionId") String subscriptionId) throws BrokerException {
        log.info("jsonrpc protocol unSubscribe interface subscriptionId:{}", subscriptionId);
        return this.masterJob.doUnsubscribe(WeEventConstants.JSONRPCTYPE, subscriptionId, "");
    }

    @Override
    public boolean open(@JsonRpcParam(value = "topic") String topic,
                        @JsonRpcParam(value = "groupId") String groupId) throws BrokerException {
        log.info("jsonrpc protocol open interface topic:{} groupId:{}", topic, groupId);
        return this.producer.open(topic, groupId);
    }

    @Override
    public boolean open(@JsonRpcParam(value = "topic") String topic) throws BrokerException {
        log.info("jsonrpc protocol open interface topic:{}", topic);
        return this.producer.open(topic, WeEvent.DEFAULT_GROUP_ID);
    }

    @Override
    public boolean close(@JsonRpcParam(value = "topic") String topic,
                         @JsonRpcParam(value = "groupId") String groupId) throws BrokerException {
        log.info("jsonrpc protocol close interface topic:{} groupId:{}", topic, groupId);
        return this.producer.close(topic, groupId);
    }

    @Override
    public boolean close(@JsonRpcParam(value = "topic") String topic) throws BrokerException {
        log.info("jsonrpc protocol close interface topic:{}", topic);
        return this.producer.close(topic, WeEvent.DEFAULT_GROUP_ID);
    }

    @Override
    public boolean exist(@JsonRpcParam(value = "topic") String topic,
                         @JsonRpcParam(value = "groupId") String groupId) throws BrokerException {
        log.info("jsonrpc protocol exist interface topic:{} groupId:{}", topic, groupId);
        return this.producer.exist(topic, groupId);
    }

    @Override
    public boolean exist(@JsonRpcParam(value = "topic") String topic) throws BrokerException {
        log.info("jsonrpc protocol exist interface topic:{} groupId", topic);
        return this.producer.exist(topic, WeEvent.DEFAULT_GROUP_ID);
    }

    @Override
    public TopicPage list(@JsonRpcParam(value = "pageIndex") Integer pageIndex,
                          @JsonRpcParam(value = "pageSize") Integer pageSize,
                          @JsonRpcParam(value = "groupId") String groupId) throws BrokerException {
        log.info("jsonrpc protocol list interface pageIndex:{} pageSize:{} groupId:{}", pageIndex, pageSize, groupId);
        return this.producer.list(pageIndex, pageSize, groupId);
    }

    @Override
    public TopicPage list(@JsonRpcParam(value = "pageIndex") Integer pageIndex,
                          @JsonRpcParam(value = "pageSize") Integer pageSize) throws BrokerException {
        log.info("jsonrpc protocol list interface pageIndex:{} pageSize:{}", pageIndex, pageSize);
        return this.producer.list(pageIndex, pageSize, WeEvent.DEFAULT_GROUP_ID);
    }

    @Override
    public TopicInfo state(@JsonRpcParam(value = "topic") String topic,
                           @JsonRpcParam(value = "groupId") String groupId) throws BrokerException {
        log.info("jsonrpc protocol state interface topic:{} groupId:{}", topic, groupId);
        return this.producer.state(topic, groupId);
    }

    @Override
    public TopicInfo state(@JsonRpcParam(value = "topic") String topic) throws BrokerException {
        log.info("jsonrpc protocol state interface topic:{}", topic);
        return this.producer.state(topic, WeEvent.DEFAULT_GROUP_ID);
    }
}
