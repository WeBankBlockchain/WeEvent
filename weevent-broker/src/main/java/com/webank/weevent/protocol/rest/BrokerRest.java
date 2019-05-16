package com.webank.weevent.protocol.rest;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import com.webank.weevent.broker.fisco.constant.WeEventConstants;
import com.webank.weevent.broker.fisco.util.WeEventUtils;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.broker.plugin.IConsumer;
import com.webank.weevent.broker.plugin.IProducer;
import com.webank.weevent.sdk.SendResult;
import com.webank.weevent.sdk.TopicInfo;
import com.webank.weevent.sdk.TopicPage;
import com.webank.weevent.sdk.WeEvent;
import com.webank.weevent.sdk.jsonrpc.IBrokerRpc;

import lombok.extern.slf4j.Slf4j;
import org.junit.runners.Parameterized;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.webank.weevent.sdk.ErrorCode.EVENT_EXTENSIONS_GROUP_ID_NOT_FOUND;

/**
 * Implement of Restful service.
 * Client access over restful api only, no client sdk.
 * 1. Support both get and post method.
 * 2. All input and output params are in json style.
 *
 * @author matthewliu
 * @since 2018/11/22
 */
@Slf4j
@RequestMapping(value = "/rest")
@RestController
public class BrokerRest extends RestHA implements IBrokerRpc {
    private IProducer producer;
    private IConsumer consumer;

    @Autowired
    public void setProducer(IProducer producer) {
        this.producer = producer;
    }

    @Autowired
    public void setConsumer(IConsumer consumer) {
        this.consumer = consumer;
    }

    @Override
    @RequestMapping(path = "/publish")
    public SendResult publish(@RequestParam Map<String, String> eventData) throws BrokerException {
        log.debug("topic: {}, content.length: {}", eventData.get("topic"), eventData.get("content").getBytes().length);
        Map<String, String> extensions = WeEventUtils.getExtensions(eventData);
        WeEvent event = new WeEvent(eventData.get(WeEventConstants.TOPIC_SPLIT_ID), eventData.get(WeEventConstants.CONTENT_SPLIT_ID).getBytes(), extensions);
        return this.producer.publish(event);
    }

    @Override
    @RequestMapping(path = "/subscribe")
    public String subscribe(@RequestParam(name = "topic") String topic,
                            @RequestParam(name = "subscriptionId", required = false) String subscriptionId,
                            @RequestParam(name = "url") String url,
                            @RequestParam(name = "groupId", required = false) Long groupId) throws BrokerException {
        checkSupport();
        return this.masterJob.getCgiSubscription().restSubscribe(topic,
                groupId,
                subscriptionId,
                url,
                getUrlFormat(this.request));
    }

    @Override
    @RequestMapping(path = "/unSubscribe")
    public boolean unSubscribe(@RequestParam(name = "subscriptionId") String subscriptionId,
                               @RequestParam(name = "groupId", required = false) Long groupId) throws BrokerException {
        checkSupport();
        return this.masterJob.getCgiSubscription().restUnsubscribe(subscriptionId, getUrlFormat(this.request));
    }

    @Override
    @RequestMapping(path = "/getEvent")
    public WeEvent getEvent(@RequestParam(name = "eventId") String eventId,
                            @RequestParam(name = "groupId", required = false) Long groupId) throws BrokerException {
        return this.producer.getEvent(eventId, groupId);
    }

    @Override
    @RequestMapping(path = "/open")
    public boolean open(@RequestParam(name = "topic") String topic,
                        @RequestParam(name = "groupId", required = false) Long groupId) throws BrokerException {
        return this.producer.open(topic, groupId);
    }

    @Override
    @RequestMapping(path = "/close")
    public boolean close(@RequestParam(name = "topic") String topic,
                         @RequestParam(name = "groupId", required = false) Long groupId) throws BrokerException {
        return this.producer.close(topic, groupId);
    }

    @Override
    @RequestMapping(path = "/exist")
    public boolean exist(@RequestParam(name = "topic") String topic,
                         @RequestParam(name = "groupId", required = false) Long groupId) throws BrokerException {
        return this.producer.exist(topic, groupId);
    }

    @Override
    @RequestMapping(path = "/list")
    public TopicPage list(@RequestParam(name = "pageIndex") Integer pageIndex,
                          @RequestParam(name = "pageSize") Integer pageSize,
                          @RequestParam(name = "groupId", required = false) Long groupId) throws BrokerException {
        return this.producer.list(pageIndex, pageSize, groupId);
    }

    @Override
    @RequestMapping(path = "/state")
    public TopicInfo state(@RequestParam(name = "topic") String topic,
                           @RequestParam(name = "groupId", required = false) Long groupId) throws BrokerException {
        return this.producer.state(topic, groupId);
    }
}
