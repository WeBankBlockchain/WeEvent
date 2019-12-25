package com.webank.weevent.broker.fisco.util;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.webank.weevent.broker.fisco.constant.WeEventConstants;
import com.webank.weevent.broker.fisco.dto.ListPage;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.ErrorCode;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapLikeType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.util.encoders.Hex;

/**
 * Data type conversion utilities between solidity data type and java data type.
 */
@Slf4j
public final class DataTypeUtils {

    private static String STRING_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static Map<String, String> topicHashMap = new ConcurrentHashMap<>();
    private static ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    static {
        // Include.NON_NULL Property is NULL and not serialized
        OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        //Do not convert inconsistent fields
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
    /**
     * encode eventId
     *
     * @param topicName topic name
     * @param eventBlockNumber block chain block number
     * @param eventSeq eventSeq number
     * @return encodeString
     */
    public static String encodeEventId(String topicName, int eventBlockNumber, int eventSeq) {
        StringBuilder sb = new StringBuilder();
        sb.append(genTopicNameHash(topicName));
        sb.append(WeEventConstants.EVENT_ID_SPLIT_CHAR);
        sb.append(eventSeq);
        sb.append(WeEventConstants.EVENT_ID_SPLIT_CHAR);
        sb.append(eventBlockNumber);
        return sb.toString();
    }

    /**
     * generate topicName hash
     *
     * @param topicName topic name
     * @return substring left 4bit hash data to hex encode
     */
    public static String genTopicNameHash(String topicName) {
        if (topicHashMap.containsKey(topicName)) {
            return topicHashMap.get(topicName);
        }

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] messageDigest = md.digest(topicName.getBytes());
            String hash = new String(Hex.encode(messageDigest)).substring(0, WeEventConstants.TOPIC_NAME_ENCODE_LENGTH);
            topicHashMap.put(topicName, hash);

            log.info("topic name hash: {} <-> {}", topicName, hash);
            return hash;
        } catch (NoSuchAlgorithmException e) {
            log.error("NoSuchAlgorithmException:{}", e.getMessage());
            return "";
        }
    }

    /**
     * decode eventId get seq
     *
     * @param eventId event id
     * @return seq
     */
    public static Long decodeSeq(String eventId) throws BrokerException {
        String[] tokens = eventId.split(WeEventConstants.EVENT_ID_SPLIT_CHAR);
        if (tokens.length != 3) {
            throw new BrokerException(ErrorCode.EVENT_ID_IS_ILLEGAL);
        }

        if (tokens[0].length() != WeEventConstants.TOPIC_NAME_ENCODE_LENGTH) {
            throw new BrokerException(ErrorCode.EVENT_ID_IS_ILLEGAL);
        }
        return DataTypeUtils.stringToLong(tokens[1]);
    }

    /**
     * decode eventId get blockNumber
     *
     * @param eventId event id
     * @return blockNumber
     */
    public static Long decodeBlockNumber(String eventId) throws BrokerException {
        String[] tokens = eventId.split(WeEventConstants.EVENT_ID_SPLIT_CHAR);
        if (tokens.length != 3) {
            throw new BrokerException(ErrorCode.EVENT_ID_IS_ILLEGAL);
        }
        if (tokens[0].length() != WeEventConstants.TOPIC_NAME_ENCODE_LENGTH) {
            throw new BrokerException(ErrorCode.EVENT_ID_IS_ILLEGAL);
        }
        return DataTypeUtils.stringToLong(tokens[2]);
    }

    /**
     * decode eventId get topicName hash
     *
     * @param eventId event id
     * @return topicName hash
     */
    public static String decodeTopicNameHash(String eventId) throws BrokerException {
        String[] tokens = eventId.split(WeEventConstants.EVENT_ID_SPLIT_CHAR);
        if (tokens.length != 3) {
            throw new BrokerException(ErrorCode.EVENT_ID_IS_ILLEGAL);
        }
        if (tokens[0].length() != WeEventConstants.TOPIC_NAME_ENCODE_LENGTH) {
            throw new BrokerException(ErrorCode.EVENT_ID_IS_ILLEGAL);
        }
        return tokens[0];
    }

    /**
     * String2Long, return 0L if exception.
     *
     * @param value the value
     * @return java.lang.Long
     */
    public static Long stringToLong(String value) {
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException e) {
            return 0L;
        }
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
     * convert data timestamp to String.
     *
     * @param date the date
     * @return the String timestamp
     */
    public static String getTimestamp(Date date) {
        return getDefaultDateFormat().format(date);
    }

    /**
     * convert long timestamp to String.
     *
     * @param date the date
     * @return the String timestamp
     */
    public static String getTimestamp(long date) {
        return getDefaultDateFormat().format(date);
    }

    /**
     * Gets the default date format.
     *
     * @return the default date format
     */
    private static DateFormat getDefaultDateFormat() {
        return new SimpleDateFormat(STRING_DATE_FORMAT);
    }

    /**
     * convert object to String
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

    /**
     * convert jsonString to ListPage<T>
     * @param jsonString json data
     * @param tclass T which in ListPage<T>
     * @param <T> ListPage
     * @return ListPage<T>
     * @throws BrokerException
     */
    public static <T> ListPage<T> json2ListPage(String jsonString, Class tclass) throws BrokerException {
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        JavaType javaType = OBJECT_MAPPER.getTypeFactory().constructParametricType(ListPage.class, tclass);
        ListPage<T> listPage = null;
        try {
            listPage = OBJECT_MAPPER.readValue(jsonString, javaType);
        } catch (IOException e) {
            log.error("convert jsonString to object failed ", e);
            throw new BrokerException(ErrorCode.JSON_DECODE_EXCEPTION);
        }

        return listPage;
    }
}
