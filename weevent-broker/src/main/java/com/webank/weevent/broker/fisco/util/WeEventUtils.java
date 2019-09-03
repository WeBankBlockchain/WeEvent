package com.webank.weevent.broker.fisco.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
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
                extensions.put(extension.getKey(), getValue(extension));
            }
        }
        return extensions;
    }

    private static String getValue(Map.Entry<String, String> extension) {
        if (extension.getValue() == null) {
            return null;
        }
        List<Object> list = Arrays.asList(extension.getValue());
        Object object = list.get(0);
        LinkedList linkedList = null;
        if (object instanceof LinkedList) {
            linkedList = (LinkedList) object;
        }
        String returnValue = linkedList == null ? extension.getValue() : linkedList.get(0).toString();
        return returnValue;
    }
}
