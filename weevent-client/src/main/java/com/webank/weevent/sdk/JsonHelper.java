package com.webank.weevent.sdk;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapLikeType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public class JsonHelper {

    private static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        // Include.NON_NULL Property is NULL and not serialized
        OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        //Do not convert inconsistent fields
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static Map<String, String> json2Map(String json) {
        if (StringUtils.isBlank(json)) {
            return null;
        }
        try {
            MapLikeType mapLikeType = OBJECT_MAPPER.getTypeFactory().constructMapLikeType(Map.class, String.class, String.class);
            return OBJECT_MAPPER.readValue(json, mapLikeType);
        } catch (Exception e) {
            log.error("parse extensions failed");
            return null;
        }
    }

    /**
     * convert object to String
     *
     * @param object
     * @return json data
     * @throws BrokerException
     */
    public static String object2Json(Object object) throws BrokerException {
        try {
            return OBJECT_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("convert object to String failed ", e);
            throw new BrokerException(ErrorCode.JSON_ENCODE_EXCEPTION);
        }
    }

    /**
     * convert jsonString to object
     *
     * @param jsonString json data
     * @param valueType
     * @param <T>
     * @return Object
     * @throws BrokerException
     */
    public static <T> T json2Object(String jsonString, Class<T> valueType) throws BrokerException {
        try {
            OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return OBJECT_MAPPER.readValue(jsonString, valueType);
        } catch (IOException e) {
            log.error("convert jsonString to object failed ", e);
            throw new BrokerException(ErrorCode.JSON_DECODE_EXCEPTION);
        }
    }
}
