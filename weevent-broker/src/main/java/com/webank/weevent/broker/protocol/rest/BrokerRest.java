package com.webank.weevent.broker.protocol.rest;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.webank.weevent.client.BaseResponse;
import com.webank.weevent.client.BrokerException;
import com.webank.weevent.client.ErrorCode;
import com.webank.weevent.client.SendResult;
import com.webank.weevent.client.TopicInfo;
import com.webank.weevent.client.TopicPage;
import com.webank.weevent.client.WeEvent;
import com.webank.weevent.core.IProducer;
import com.webank.weevent.core.fisco.constant.WeEventConstants;
import com.webank.weevent.core.fisco.util.WeEventUtils;

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
public class BrokerRest {
    private IProducer producer;

    @Autowired
    public void setProducer(IProducer producer) {
        this.producer = producer;
    }

    @RequestMapping(path = "/publish")
    public CompletableFuture<SendResult> publish(@RequestParam Map<String, String> eventData) throws BrokerException {
        if (!eventData.containsKey(WeEventConstants.EVENT_TOPIC)
                || !eventData.containsKey(WeEventConstants.EVENT_CONTENT)) {
            log.error("miss param");
            throw new BrokerException(ErrorCode.URL_INVALID_FORMAT);
        }

        log.info("topic: {}, content.length: {}",
                eventData.get(WeEventConstants.EVENT_TOPIC),
                eventData.get(WeEventConstants.EVENT_CONTENT).getBytes().length);

        WeEvent event = new WeEvent(eventData.get(WeEventConstants.EVENT_TOPIC),
                eventData.get(WeEventConstants.EVENT_CONTENT).getBytes(),
                WeEventUtils.getExtensions(eventData));

        // get group id
        String groupId = "";
        if (eventData.containsKey(WeEventConstants.EVENT_GROUP_ID)) {
            groupId = eventData.get(WeEventConstants.EVENT_GROUP_ID);
        }
        return this.producer.publish(event, groupId);
    }

    @RequestMapping(path = "/getEvent")
    public BaseResponse<WeEvent> getEvent(@RequestParam(name = "eventId") String eventId,
                                          @RequestParam(name = "groupId", required = false) String groupId) throws BrokerException {
        String decodeEventId;
        try {
            decodeEventId = URLDecoder.decode(eventId, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            log.error("decode eventId error", e);
            throw new BrokerException(ErrorCode.DECODE_EVENT_ID_ERROR);
        }
        log.info("eventId:{} groupId:{}", decodeEventId, groupId);

        return BaseResponse.buildSuccess(this.producer.getEvent(decodeEventId, groupId));
    }

    @RequestMapping(path = "/open")
    public BaseResponse<Boolean> open(@RequestParam(name = "topic") String topic,
                                      @RequestParam(name = "groupId", required = false) String groupId) throws BrokerException {
        String decodeTopic;
        try {
            decodeTopic = URLDecoder.decode(topic, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            log.error("decode topic error", e);
            throw new BrokerException(ErrorCode.DECODE_TOPIC_ERROR);
        }
        log.info("topic:{} groupId:{}", decodeTopic, groupId);

        return BaseResponse.buildSuccess(this.producer.open(decodeTopic, groupId));
    }

    @RequestMapping(path = "/close")
    public BaseResponse<Boolean> close(@RequestParam(name = "topic") String topic,
                                       @RequestParam(name = "groupId", required = false) String groupId) throws BrokerException {
        String decodeTopic;
        try {
            decodeTopic = URLDecoder.decode(topic, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            log.error("decode topic error", e);
            throw new BrokerException(ErrorCode.DECODE_TOPIC_ERROR);
        }
        log.info("topic:{} groupId:{}", decodeTopic, groupId);

        return BaseResponse.buildSuccess(this.producer.close(decodeTopic, groupId));
    }

    @RequestMapping(path = "/exist")
    public BaseResponse<Boolean> exist(@RequestParam(name = "topic") String topic,
                                       @RequestParam(name = "groupId", required = false) String groupId) throws BrokerException {
        String decodeTopic;
        try {
            decodeTopic = URLDecoder.decode(topic, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            log.error("decode topic error", e);
            throw new BrokerException(ErrorCode.DECODE_TOPIC_ERROR);
        }
        log.info("topic:{} groupId:{}", decodeTopic, groupId);

        return BaseResponse.buildSuccess(this.producer.exist(decodeTopic, groupId));
    }

    @RequestMapping(path = "/list")
    public BaseResponse<TopicPage> list(@RequestParam(name = "pageIndex") Integer pageIndex,
                                        @RequestParam(name = "pageSize") Integer pageSize,
                                        @RequestParam(name = "groupId", required = false) String groupId) throws BrokerException {
        log.info("pageIndex:{} pageSize:{} groupId:{}", pageIndex, pageSize, groupId);

        return BaseResponse.buildSuccess(this.producer.list(pageIndex, pageSize, groupId));
    }

    @RequestMapping(path = "/state")
    public BaseResponse<TopicInfo> state(@RequestParam(name = "topic") String topic,
                                         @RequestParam(name = "groupId", required = false) String groupId) throws BrokerException {
        String decodeTopic;
        try {
            decodeTopic = URLDecoder.decode(topic, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            log.error("decode topic error", e);
            throw new BrokerException(ErrorCode.DECODE_TOPIC_ERROR);
        }
        log.info("topic:{} groupId:{}", decodeTopic, groupId);

        return BaseResponse.buildSuccess(this.producer.state(decodeTopic, groupId));
    }
}
