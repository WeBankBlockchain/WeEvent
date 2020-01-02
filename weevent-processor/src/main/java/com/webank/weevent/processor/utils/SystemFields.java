package com.webank.weevent.processor.utils;

import java.util.Arrays;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public enum SystemFields {
    EVENT_ID("eventId"),
    TOPIC_NAME("topicName"),
    BROKER_ID("brokerId"),
    GROUP_ID("groupId"),
    NOW("now"),
    CURRENT_DATE("currentDate"),
    CURRENT_TIME("currentTime");

    public final String field;

    private SystemFields(String field) {
        this.field = field;
    }

    public String getField() {
        return field;
    }

    public String toString() {
        return "SystemFields[ name=" + field + "]";
    }

    public static SystemFields getByClassCodeAndInfoCode(String infoCode) {
        log.info("field:{}", SystemFields.valueOf("EVENT_ID"));
        Optional<SystemFields> opt = Arrays.stream(SystemFields.values()).filter(item -> item.field.equals(infoCode)).findFirst();
        return opt.orElse(null);
    }
}
