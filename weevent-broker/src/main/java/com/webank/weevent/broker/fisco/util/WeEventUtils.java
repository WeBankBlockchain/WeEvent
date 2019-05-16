package com.webank.weevent.broker.fisco.util;

import java.util.HashMap;
import java.util.Map;

import com.webank.weevent.broker.fisco.constant.WeEventConstants;
import com.webank.weevent.sdk.BrokerException;

import static com.webank.weevent.sdk.ErrorCode.EVENT_EXTENSIONS_GROUP_ID_NOT_FOUND;

public class WeEventUtils {
    public static Map<String, String> getExtensions(Map<String, String> eventData) throws BrokerException {
        Map<String, String> extensions = new HashMap<>();
        for (Map.Entry<String, String> extension : eventData.entrySet()) {
            if (extension.getKey().contains(WeEventConstants.EXTENSIONS_SPLIT_CHAR)) {
                extensions.put(extension.getKey(), extension.getValue());
            }
        }
        if (!eventData.containsKey(WeEventConstants.EXTENSIONS_GROUP_ID)) {
            throw new BrokerException(EVENT_EXTENSIONS_GROUP_ID_NOT_FOUND);
        }
        return extensions;
    }

    public static Map<String, String> getObjectExtensions(Map<String, Object> eventData) throws BrokerException {
        Map<String, String> extensions = new HashMap<>();
        for (Map.Entry<String, Object> extension : eventData.entrySet()) {
            if (extension.getKey().contains(WeEventConstants.EXTENSIONS_SPLIT_CHAR)) {
                extensions.put(extension.getKey(), extension.getValue().toString());
            }
        }
        if (!eventData.containsKey(WeEventConstants.EXTENSIONS_GROUP_ID)) {
            throw new BrokerException(EVENT_EXTENSIONS_GROUP_ID_NOT_FOUND);
        }
        return extensions;
    }
}
