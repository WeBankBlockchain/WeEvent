package com.webank.weevent.broker.fisco.util;

import java.util.HashMap;
import java.util.Map;

import com.webank.weevent.broker.fisco.constant.WeEventConstants;
import com.webank.weevent.sdk.BrokerException;

import lombok.extern.slf4j.Slf4j;

import static com.webank.weevent.sdk.ErrorCode.EVENT_GROUP_ID_INVALID;
@Slf4j
public class WeEventUtils {
    public static Map<String, String> getExtensions(Map<String, String> eventData) throws BrokerException {
        Map<String, String> extensions = new HashMap<>();
        for (Map.Entry<String, String> extension : eventData.entrySet()) {
            if (extension.getKey().startsWith(WeEventConstants.EXTENSIONS_PREFIX_CHAR)) {
                extensions.put(extension.getKey(), extension.getValue());
            }
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
        return extensions;
    }
}
