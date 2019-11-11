package com.webank.weevent.protocol.rest;

import java.util.List;
import java.util.Map;

import com.webank.weevent.broker.fisco.constant.WeEventConstants;
import com.webank.weevent.broker.fisco.util.WeEventUtils;
import com.webank.weevent.broker.plugin.IProducer;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.ErrorCode;
import com.webank.weevent.sdk.SendResult;
import com.webank.weevent.sdk.TopicInfo;
import com.webank.weevent.sdk.TopicPage;
import com.webank.weevent.sdk.WeEvent;
import com.webank.weevent.sdk.jsonrpc.IBrokerRpc;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
public class BrokerRest implements IBrokerRpc {
    private IProducer producer;

    @Autowired
    public void setProducer(IProducer producer) {
        this.producer = producer;
    }

    @RequestMapping(path = "/publish")
    public SendResult publish(@RequestParam Map<String, String> eventData) throws BrokerException {
        if (!eventData.containsKey(WeEventConstants.EVENT_TOPIC)
                || !eventData.containsKey(WeEventConstants.EVENT_CONTENT)) {
            log.error("miss param");
            throw new BrokerException(ErrorCode.URL_INVALID_FORMAT);
        }

        log.info("topic: {}, content.length: {}",
                eventData.get(WeEventConstants.EVENT_TOPIC),
                eventData.get(WeEventConstants.EVENT_CONTENT).getBytes().length);

        Map<String, String> extensions = WeEventUtils.getExtensions(eventData);
        WeEvent event = new WeEvent(eventData.get(WeEventConstants.EVENT_TOPIC), eventData.get(WeEventConstants.EVENT_CONTENT).getBytes(), extensions);

        // get group id
        String groupId = "";
        if (eventData.containsKey(WeEventConstants.EVENT_GROUP_ID)) {
            groupId = eventData.get(WeEventConstants.EVENT_GROUP_ID);
        }
        return this.producer.publish(event, groupId);
    }

    @Override
    @RequestMapping(path = "/getEvent")
    public WeEvent getEvent(@RequestParam(name = "eventId") String eventId,
                            @RequestParam(name = "groupId", required = false) String groupId) throws BrokerException {
        log.info("eventId:{} groupId:{}", eventId, groupId);

        return this.producer.getEvent(eventId, groupId);
    }

    @Override
    @RequestMapping(path = "/open")
    public boolean open(@RequestParam(name = "topic") String topic,
                        @RequestParam(name = "groupId", required = false) String groupId) throws BrokerException {
        log.info("topic:{} groupId:{}", topic, groupId);

        return this.producer.open(topic, groupId);
    }

    @Override
    @RequestMapping(path = "/close")
    public boolean close(@RequestParam(name = "topic") String topic,
                         @RequestParam(name = "groupId", required = false) String groupId) throws BrokerException {
        log.info("topic:{} groupId:{}", topic, groupId);

        return this.producer.close(topic, groupId);
    }

    @Override
    @RequestMapping(path = "/exist")
    public boolean exist(@RequestParam(name = "topic") String topic,
                         @RequestParam(name = "groupId", required = false) String groupId) throws BrokerException {
        log.info("topic:{} groupId:{}", topic, groupId);

        return this.producer.exist(topic, groupId);
    }

    @Override
    @RequestMapping(path = "/list")
    public TopicPage list(@RequestParam(name = "pageIndex") Integer pageIndex,
                          @RequestParam(name = "pageSize") Integer pageSize,
                          @RequestParam(name = "groupId", required = false) String groupId) throws BrokerException {
        log.info("pageIndex:{} pageSize:{} groupId:{}", pageIndex, pageSize, groupId);

        return this.producer.list(pageIndex, pageSize, groupId);
    }

    @Override
    @RequestMapping(path = "/state")
    public TopicInfo state(@RequestParam(name = "topic") String topic,
                           @RequestParam(name = "groupId", required = false) String groupId) throws BrokerException {
        log.info("topic:{} groupId:{}", topic, groupId);

        return this.producer.state(topic, groupId);
    }
}
