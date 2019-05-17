package com.webank.weevent.broker.fisco.util;

import java.util.HashMap;
import java.util.Map;

import com.webank.weevent.broker.config.FiscoConfig;
import com.webank.weevent.broker.fisco.constant.WeEventConstants;
import com.webank.weevent.sdk.BrokerException;

import org.springframework.beans.factory.annotation.Autowired;

import static com.webank.weevent.sdk.ErrorCode.EVENT_CONTENT_EXCEEDS_MAX_LENGTH;
import static com.webank.weevent.sdk.ErrorCode.EVENT_EXTENSIONS_GROUP_ID_INVALID;
import static com.webank.weevent.sdk.ErrorCode.EVENT_EXTENSIONS_GROUP_ID_NOT_FOUND;

public class WeEventUtils {
    public static Map<String, String> getExtensions(Map<String, String> eventData) throws BrokerException {
        Map<String, String> extensions = new HashMap<>();
        for (Map.Entry<String, String> extension : eventData.entrySet()) {
            if (extension.getKey().startsWith(WeEventConstants.EXTENSIONS_PREFIX_CHAR)) {
                extensions.put(extension.getKey(), extension.getValue());
            }
        }
        if (eventData.containsKey(WeEventConstants.EXTENSIONS_GROUP_ID)) {
            getGroupId(eventData.get(WeEventConstants.EXTENSIONS_GROUP_ID).toString());
        } else {
            extensions.put(WeEventConstants.EXTENSIONS_GROUP_ID, WeEventConstants.EXTENSIONS_DEFAULT_GROUP_ID);
        }
        return extensions;
    }

    public static Map<String, String> getObjectExtensions(Map<String, Object> eventData) throws BrokerException {
        Map<String, String> extensions = new HashMap<>();
        for (Map.Entry<String, Object> extension : eventData.entrySet()) {
            if (extension.getKey().startsWith(WeEventConstants.EXTENSIONS_PREFIX_CHAR)) {
                extensions.put(extension.getKey(), extension.getValue().toString());
            }
        }
        if (eventData.containsKey(WeEventConstants.EXTENSIONS_GROUP_ID)) {
            getGroupId(eventData.get(WeEventConstants.EXTENSIONS_GROUP_ID).toString());
        } else {
            extensions.put(WeEventConstants.EXTENSIONS_GROUP_ID, WeEventConstants.EXTENSIONS_DEFAULT_GROUP_ID);
        }

        return extensions;
    }

    public static Long getGroupId(String strGroupId) throws BrokerException {
        Long groupId = 1L;
        if (strGroupId != null) {
            try {
                groupId = Long.parseLong(strGroupId);
            } catch (Exception e) {
                throw new BrokerException(EVENT_EXTENSIONS_GROUP_ID_INVALID);
            }
        }
        return groupId;
    }
}
