package com.webank.weevent.processor.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.webank.weevent.sdk.JsonHelper;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.type.MapLikeType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;


@Slf4j
public class JsonUtil {

    static {
        // Include.NON_NULL Property is NULL and not serialized
        JsonHelper.getObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        //Do not convert inconsistent fields
        JsonHelper.getObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static <T> String toJSONString(T data) throws IOException {
        Assert.notNull(data, "data is null");
        try {
            return JsonHelper.getObjectMapper().writeValueAsString(data);
        } catch (JsonProcessingException e) {
            log.error("conversion of Json failed", e);
            throw new IOException("conversion of Json failed", e);
        }
    }

    public static <T> T parseObject(String data, Class<T> tClass) throws IOException {
        Assert.hasText(data, "data without text");
        try {
            return JsonHelper.getObjectMapper().readValue(data, tClass);
        } catch (JsonProcessingException e) {
            log.error("conversion of Json failed", e);
            throw new IOException("conversion of Json failed", e);
        }
    }

    public static <T, R> Map<T, R> parseObjectToMap(String data, Class tclass1, Class tclass2) throws IOException {
        if (StringUtils.isBlank(data)) {
            return new HashMap<>();
        }
        try {
            MapLikeType mapLikeType = JsonHelper.getObjectMapper().getTypeFactory().constructMapLikeType(Map.class, tclass1, tclass2);
            return JsonHelper.getObjectMapper().readValue(data, mapLikeType);
        } catch (Exception e) {
            log.error("conversion of Json failed", e);
            throw new IOException("conversion of Json failed", e);
        }
    }


    public static Map<String, Object> parseObjectToMap(String data) throws IOException {
        Assert.hasText(data, "data without text");
        try {
            MapLikeType mapLikeType = JsonHelper.getObjectMapper().getTypeFactory().constructMapLikeType(Map.class, String.class, Object.class);
            return JsonHelper.getObjectMapper().readValue(data, mapLikeType);
        } catch (Exception e) {
            log.error("conversion of Json failed", e);
            throw new IOException("conversion of Json failed", e);
        }
    }


    public static boolean isValid(String data) {
        Assert.hasText(data, "data without text");
        try {
            JsonHelper.getObjectMapper().readTree(data);
            return true;
        } catch (IOException e) {
            return false;
        }
    }


}
