package com.webank.weevent.broker.fisco.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import com.webank.weevent.broker.fisco.constant.WeEventConstants;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.ErrorCode;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.util.encoders.Hex;

/**
 * Data type conversion utilities between solidity data type and java data type.
 */
@Slf4j
public final class DataTypeUtils {

    private static String STRING_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

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
        String encodeData = "";
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] messageDigest = md.digest(topicName.getBytes());
            encodeData = new String(Hex.encode(messageDigest)).substring(0, WeEventConstants.TOPIC_NAME_ENCODE_LENGTH);
        } catch (NoSuchAlgorithmException e) {
            log.error("NoSuchAlgorithmException:{}", e.getMessage());
        }
        return encodeData;
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

    @SuppressWarnings("unchecked")
    public static Map<String, String> json2Map(String json) {
        if (StringUtils.isBlank(json)) {
            return null;
        }

        try {
            return (Map<String, String>) JSON.parse(json);
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

}
