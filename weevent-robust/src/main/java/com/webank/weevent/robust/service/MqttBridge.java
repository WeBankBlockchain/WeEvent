package com.webank.weevent.robust.service;

import java.util.Date;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;

@Slf4j
public class MqttBridge implements MessageHandler {

    private Integer countMqtt = 1;

    @Override
    public void handleMessage(Message<?> message) throws MessagingException {
        Object payload = message.getPayload();
        Map map = JSONObject.parseObject(payload.toString(), Map.class);
        if (map.get("eventId") != null) {
            String eventId = map.get("eventId").toString();
            log.info("mqtt receive success.topic {}, eventId: {},countMqtt:{}", map.get("topic"), eventId, countMqtt++);
            ScheduledService.countTimes(ScheduledService.getMqttReceiveMap(), DateFormatUtils.format(new Date(), "yyyy-MM-dd HH"));
            }
    }
}
