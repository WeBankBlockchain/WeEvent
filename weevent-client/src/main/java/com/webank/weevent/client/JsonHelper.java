package com.webank.weevent.client;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
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
            log.error("json encode failed", e);
            throw new BrokerException(ErrorCode.JSON_ENCODE_EXCEPTION);
        }
    }

    /**
     * convert object to String
     *
     * @param object java object
     * @return json data
     * @throws BrokerException BrokerException
     */
    public static <T> T object2Dto(Object object, TypeReference<T> typeReference) throws BrokerException {
        return OBJECT_MAPPER.convertValue(object, typeReference);
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
            log.error("json encode failed", e);
            throw new BrokerException(ErrorCode.JSON_ENCODE_EXCEPTION);
        }
    }

    /**
     * convert jsonString to object
     *
     * @param jsonString json String
     * @param valueType java object type
     * @param <T> template type
     * @return Object java object
     * @throws BrokerException BrokerException
     */
    public static <T> T json2Object(String jsonString, Class<T> valueType) throws BrokerException {
        try {
            return OBJECT_MAPPER.readValue(jsonString, valueType);
        } catch (IOException e) {
            log.error("json decode failed", e);
            throw new BrokerException(ErrorCode.JSON_DECODE_EXCEPTION);
        }
    }

    /**
     * convert json String to Object
     *
     * @param jsonString json String
     * @param typeReference typeReference
     * @param <T> template type
     * @return class instance
     * @throws BrokerException BrokerException
     */
    public static <T> T json2Object(String jsonString, TypeReference<T> typeReference) throws BrokerException {
        try {
            return OBJECT_MAPPER.readValue(jsonString, typeReference);
        } catch (IOException e) {
            log.error("json decode failed", e);
            throw new BrokerException(ErrorCode.JSON_DECODE_EXCEPTION);
        }
    }

    /**
     * convert json byte[] to Object
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
            log.error("json decode failed", e);
            throw new BrokerException(ErrorCode.JSON_DECODE_EXCEPTION);
        }
    }

    /**
     * convert json byte[] to Object
     *
     * @param json json data
     * @param typeReference typeReference
     * @param <T> template type
     * @return class instance
     * @throws BrokerException BrokerException
     */
    public static <T> T json2Object(byte[] json, TypeReference<T> typeReference) throws BrokerException {
        try {
            return OBJECT_MAPPER.readValue(json, typeReference);
        } catch (IOException e) {
            log.error("json decode failed", e);
            throw new BrokerException(ErrorCode.JSON_DECODE_EXCEPTION);
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
