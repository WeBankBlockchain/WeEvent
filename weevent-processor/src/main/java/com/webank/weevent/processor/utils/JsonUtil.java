package com.webank.weevent.processor.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;


@Slf4j
public class JsonUtil {


    public static <T> String toJSONString(T data) throws IOException {
        Assert.notNull(data, "data is null");
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            log.error("conversion of Json failed", e);
            throw new IOException("conversion of Json failed", e);
        }
    }

    public static <T> T parseObject(String data, Class<T> tClass) throws IOException {
        Assert.hasText(data, "data without text");
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return objectMapper.readValue(data, tClass);
        } catch (JsonProcessingException e) {
            log.error("conversion of Json failed", e);
            throw new IOException("conversion of Json failed", e);
        }
    }

    public static Map<String, Object> parseObjectToMap(String data) throws IOException {
        Assert.hasText(data, "data without text");
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(data, new HashMap<String, Object>().getClass());
        } catch (Exception e) {
            log.error("conversion of Json failed", e);
            throw new IOException("conversion of Json failed", e);
        }
    }


    public static boolean isValid(String data) {
        Assert.hasText(data, "data without text");
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.readTree(data);
            return true;
        } catch (JsonProcessingException e) {
            return false;
        }
    }


}
