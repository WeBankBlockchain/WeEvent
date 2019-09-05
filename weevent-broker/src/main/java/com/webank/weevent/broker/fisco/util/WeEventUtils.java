package com.webank.weevent.broker.fisco.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.webank.weevent.broker.fisco.constant.WeEventConstants;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WeEventUtils {
    public static Map<String, String> getExtensions(Map<String, String> eventData) {
        Map<String, String> extensions = new HashMap<>();
        for (Map.Entry<String, String> extension : eventData.entrySet()) {
            if (extension.getKey().startsWith(WeEventConstants.EXTENSIONS_PREFIX_CHAR)) {
                extensions.put(extension.getKey(), extension.getValue());
            }
        }
        return extensions;
    }

    public static Map<String, String> getExtension(Map<String, List<String>> eventData) {
        Map<String, String> extensions = new HashMap<>();
        for (Map.Entry<String, List<String>> extension : eventData.entrySet()) {
            if (extension.getKey().startsWith(WeEventConstants.EXTENSIONS_PREFIX_CHAR)) {
                extensions.put(extension.getKey(), extension.getValue().get(0));
            }
        }
        return extensions;
    }
}
