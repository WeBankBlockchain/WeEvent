package com.webank.weevent.robust.service;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
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
        ObjectMapper objectMapper = new ObjectMapper();
        Map map = null;
        try {
            map = objectMapper.readValue(payload.toString(), Map.class);
        } catch (IOException e) {
            log.error("json conversion failed", e);
            e.printStackTrace();
        }
        if (map.get("eventId") != null) {
            String eventId = map.get("eventId").toString();
            log.info("mqtt receive success.topic {}, eventId: {},countMqtt:{}", map.get("topic"), eventId, countMqtt++);
            ScheduledService.countTimes(ScheduledService.getMqttReceiveMap(), DateFormatUtils.format(new Date(), "yyyy-MM-dd HH"));
        }
    }
}
