package com.webank.weevent.client;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapLikeType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public class JsonHelper {

    private static ObjectMapper OBJECT_MAPPER;

    static {
        OBJECT_MAPPER = new ObjectMapper();

        // Include.NON_NULL Property is NULL and not serialized
        OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        // DO NOT convert inconsistent fields
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }

    /**
     * convert object to String
     *
     * @param object java object
     * @return json data
     * @throws BrokerException BrokerException
     */
    public static String object2Json(Object object) throws BrokerException {
        try {
            return OBJECT_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("convert object to jsonString failed ", e);
            throw new BrokerException(ErrorCode.JSON_ENCODE_EXCEPTION);
        }
    }

    /**
     * convert object to byte[]
     *
     * @param object java object
     * @return json data
     * @throws BrokerException BrokerException
     */
    public static byte[] object2JsonBytes(Object object) throws BrokerException {
        try {
            return OBJECT_MAPPER.writeValueAsBytes(object);
        } catch (JsonProcessingException e) {
            log.error("convert object to jsonString failed ", e);
            throw new BrokerException(ErrorCode.JSON_ENCODE_EXCEPTION);
        }
    }

    /**
     * convert jsonString to object
     *
     * @param jsonString json data
     * @param valueType java object type
     * @param <T> template type
     * @return Object java object
     * @throws BrokerException BrokerException
     */
    public static <T> T json2Object(String jsonString, Class<T> valueType) throws BrokerException {
        try {
            return OBJECT_MAPPER.readValue(jsonString, valueType);
        } catch (IOException e) {
            log.error("convert jsonString to object failed ", e);
            throw new BrokerException(ErrorCode.JSON_DECODE_EXCEPTION);
        }
    }

    /**
     * convert jsonString to object
     *
     * @param json json data
     * @param valueType java object type
     * @param <T> template type
     * @return Object java object
     * @throws BrokerException BrokerException
     */
    public static <T> T json2Object(byte[] json, Class<T> valueType) throws BrokerException {
        try {
            return OBJECT_MAPPER.readValue(json, valueType);
        } catch (IOException e) {
            log.error("convert jsonString to object failed ", e);
            throw new BrokerException(ErrorCode.JSON_DECODE_EXCEPTION);
        }
    }

    /**
     * convert json byte[] to Object
     *
     * @param json byte
     * @param clazz1 Class1
     * @param clazz2 Class2
     * @param <T> template type
     * @return class instance
     * @throws BrokerException BrokerException
     */
    public static <T> T json2Object(byte[] json, Class clazz1, Class clazz2) throws BrokerException {
        try {
            JavaType javaType = OBJECT_MAPPER.getTypeFactory().constructParametricType(clazz1, clazz2);
            return OBJECT_MAPPER.readValue(json, javaType);
        } catch (Exception e) {
            log.error("parse extensions failed");
            throw new BrokerException(ErrorCode.JSON_ENCODE_EXCEPTION);
        }
    }

    /**
     * convert object to List
     *
     * @param obj object
     * @param valueType java object type
     * @param <T> template type
     * @return class instance
     */
    public static <T> List<T> object2List(Object obj, Class<T> valueType) {
        return OBJECT_MAPPER.convertValue(obj, new TypeReference<T>() {
        });
    }

    public static <T, R> Map<T, R> json2Map(String json, Class clazz1, Class clazz2) throws BrokerException {
        try {
            MapLikeType mapLikeType = OBJECT_MAPPER.getTypeFactory().constructMapLikeType(Map.class, clazz1, clazz2);
            return OBJECT_MAPPER.readValue(json, mapLikeType);
        } catch (Exception e) {
            log.error("parse extensions failed");
            throw new BrokerException(ErrorCode.JSON_ENCODE_EXCEPTION);
        }
    }

    public static boolean isValid(String jsonString) {
        if (StringUtils.isBlank(jsonString)) {
            return false;
        }
        try {
            OBJECT_MAPPER.readTree(jsonString);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
