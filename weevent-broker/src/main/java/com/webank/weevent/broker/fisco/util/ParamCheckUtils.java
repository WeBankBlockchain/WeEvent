package com.webank.weevent.broker.fisco.util;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.UUID;

import com.webank.weevent.broker.fisco.constant.WeEventConstants;
import com.webank.weevent.broker.plugin.IConsumer;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.ErrorCode;
import com.webank.weevent.sdk.WeEvent;

import org.apache.commons.lang3.StringUtils;

/**
 * @author websterchen
 * @version 1.0
 * @since 2019/1/28
 */
public class ParamCheckUtils {
    public static void validateTopicName(String topic) throws BrokerException {
        if (StringUtils.isBlank(topic)) {
            throw new BrokerException(ErrorCode.TOPIC_IS_BLANK);
        }
        if (topic.length() > WeEventConstants.TOPIC_NAME_MAX_LENGTH) {
            throw new BrokerException(ErrorCode.TOPIC_EXCEED_MAX_LENGTH);
        }
        if (topic.endsWith(WeEventConstants.LAYER_SEPARATE)) {
            throw new BrokerException(ErrorCode.TOPIC_CONTAIN_INVALID_CHAR);
        }
        for (char x : topic.toCharArray()) {
            if (x < 32 || x > 128
                    || x == WeEventConstants.WILD_CARD_ONE_LAYER.charAt(0)
                    || x == WeEventConstants.WILD_CARD_ALL_LAYER.charAt(0)) {
                throw new BrokerException(ErrorCode.TOPIC_CONTAIN_INVALID_CHAR);
            }
        }
    }

    public static void validateOffset(String offset) throws BrokerException {
        if (StringUtils.isBlank(offset)) {
            throw new BrokerException(ErrorCode.OFFSET_IS_BLANK);
        }
    }

    public static void validateSubscriptionId(String subscriptionId) throws BrokerException {
        if (StringUtils.isBlank(subscriptionId)) {
            throw new BrokerException(ErrorCode.SUBSCRIPTIONID_IS_BLANK);
        }

        try {
            UUID.fromString(subscriptionId);
        } catch (IllegalArgumentException e) {
            throw new BrokerException(ErrorCode.SUBSCRIPTIONID_FORMAT_INVALID);
        }
    }

    public static void validateListenerNotNull(IConsumer.ConsumerListener listener) throws BrokerException {
        if (listener == null) {
            throw new BrokerException(ErrorCode.CONSUMER_LISTENER_IS_NULL);
        }
    }

    public static void validateEventId(String topicName, String eventId, Long blockHeight) throws BrokerException {
        if (StringUtils.isBlank(eventId)) {
            throw new BrokerException(ErrorCode.EVENT_ID_IS_ILLEGAL);
        }
        if (eventId.length() > WeEventConstants.EVENT_ID_MAX_LENGTH) {
            throw new BrokerException(ErrorCode.EVENT_ID_EXCEEDS_MAX_LENGTH);
        }

        if (!StringUtils.isBlank(topicName)
                && !DataTypeUtils.genTopicNameHash(topicName).equals(DataTypeUtils.decodeTopicNameHash(eventId))) {
            throw new BrokerException(ErrorCode.EVENT_ID_IS_ILLEGAL);
        }

        Long lastEventSeq = DataTypeUtils.decodeSeq(eventId);
        Long lastBlock = DataTypeUtils.decodeBlockNumber(eventId);
        if (lastEventSeq <= 0 || lastBlock <= 0) {
            throw new BrokerException(ErrorCode.EVENT_ID_IS_ILLEGAL);
        }
        if (blockHeight == 0L) {
            throw new BrokerException(ErrorCode.GET_BLOCK_HEIGHT_ERROR);
        } else if (lastBlock > blockHeight) {
            throw new BrokerException(ErrorCode.EVENT_ID_IS_MISMATCH);
        }
    }


    public static void validateEvent(WeEvent event) throws BrokerException {
        if (event.getContent() == null) {
            throw new BrokerException(ErrorCode.EVENT_CONTENT_IS_BLANK);
        }
        validateTopicName(event.getTopic());
        validateEventContent(new String(event.getContent(), StandardCharsets.UTF_8));

        if (event.getExtensions() != null) {
            if (event.getExtensions().toString().length() > WeEventConstants.EVENT_EXTENSIONS_MAX_LENGTH) {
                throw new BrokerException(ErrorCode.EVENT_EXTENSIONS_EXCEEDS_MAX_LENGTH);
            }
        }
    }

    public static void validateGroupId(String groupId, Set<Long> groups) throws BrokerException {
        Long gid;
        try {
            gid = Long.parseLong(groupId);
        } catch (Exception e) {
            throw new BrokerException(ErrorCode.EVENT_GROUP_ID_INVALID);
        }

        if (!groups.contains(gid)) {
            throw new BrokerException(ErrorCode.WE3SDK_UNKNOWN_GROUP);
        }
    }

    public static void validateEventContent(String eventContent) throws BrokerException {
        if (StringUtils.isBlank(eventContent)) {
            throw new BrokerException(ErrorCode.EVENT_CONTENT_IS_BLANK);
        }

        if (eventContent.length() > WeEventConstants.EVENT_CONTENT_MAX_LENGTH) {
            throw new BrokerException(ErrorCode.EVENT_CONTENT_EXCEEDS_MAX_LENGTH);
        }
    }

    // check format then try to connect
    public static void validateUrl(String url) throws BrokerException {
        try {
            URL conn = new URL(url);
            conn.openConnection().connect();
        } catch (MalformedURLException e) {
            throw new BrokerException(ErrorCode.URL_INVALID_FORMAT);
        } catch (IOException e) {
            throw new BrokerException(ErrorCode.URL_CONNECT_FAILED);
        }
    }

    /**
     * see WeEventUtils.match
     *
     * @param pattern topic pattern
     * @return true if yes
     */
    public static boolean isTopicPattern(String pattern) {
        if (StringUtils.isBlank(pattern)) {
            return false;
        }
        return pattern.contains("" + WeEventConstants.WILD_CARD_ALL_LAYER) || pattern.contains("" + WeEventConstants.WILD_CARD_ONE_LAYER);
    }

    /**
     * see WeEventUtils.match
     *
     * @param pattern topic pattern
     */
    public static void validateTopicPattern(String pattern) throws BrokerException {
        if (StringUtils.isBlank(pattern)) {
            throw new BrokerException(ErrorCode.PATTERN_INVALID);
        }

        if (pattern.length() > WeEventConstants.TOPIC_NAME_MAX_LENGTH) {
            throw new BrokerException(ErrorCode.TOPIC_EXCEED_MAX_LENGTH);
        }

        for (char x : pattern.toCharArray()) {
            if (x < 32 || x > 128) {
                throw new BrokerException(ErrorCode.TOPIC_CONTAIN_INVALID_CHAR);
            }
        }

        String layer[] = pattern.split(WeEventConstants.LAYER_SEPARATE);
        if (pattern.contains(WeEventConstants.WILD_CARD_ONE_LAYER)) {
            for (String x : layer) {
                if (x.contains(WeEventConstants.WILD_CARD_ONE_LAYER) && !x.equals(WeEventConstants.WILD_CARD_ONE_LAYER)) {
                    throw new BrokerException(ErrorCode.PATTERN_INVALID);
                }
            }
        } else if (pattern.contains(WeEventConstants.WILD_CARD_ALL_LAYER)) {
            // only one '#'
            if (StringUtils.countMatches(pattern, WeEventConstants.WILD_CARD_ALL_LAYER) != 1) {
                throw new BrokerException(ErrorCode.PATTERN_INVALID);
            }

            // '#' must be at last position
            if (!layer[layer.length - 1].equals(WeEventConstants.WILD_CARD_ALL_LAYER)) {
                throw new BrokerException(ErrorCode.PATTERN_INVALID);
            }
        } else {
            throw new BrokerException(ErrorCode.PATTERN_INVALID);
        }
    }
}
