package com.webank.weevent.broker.fisco.util;

import java.util.HashMap;
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

    /**
     * check is topic name matched the input pattern.
     * see MQTT specification http://public.dhe.ibm.com/software/dw/webservices/ws-mqtt/mqtt-v3r1.html.
     * notice:
     * "com/webank/weevent/" is invalid
     * "com/webank/weevent" is different from "/com.webank/weevent"
     *
     * @param topic topic name
     * @param pattern mqtt pattern with wildcard
     * @return true if match
     */
    public static boolean match(String topic, String pattern) {
        String topicLayer[] = topic.split(WeEventConstants.LAYER_SEPARATE);
        String patternLayer[] = pattern.split(WeEventConstants.LAYER_SEPARATE);

        // '+' means 1 layer
        if (pattern.contains(WeEventConstants.WILD_CARD_ONE_LAYER)) {
            if (topicLayer.length != patternLayer.length) {
                return false;
            }

            for (int idx = 0; idx < patternLayer.length; idx++) {
                // the layer except '+' must be match
                if (!patternLayer[idx].equals(WeEventConstants.WILD_CARD_ONE_LAYER)
                        && !patternLayer[idx].equals(topicLayer[idx])) {
                    return false;
                }
            }
            return true;
        } else if (pattern.contains(WeEventConstants.WILD_CARD_ALL_LAYER)) {    // '#' means 0 or n layer
            if (!patternLayer[patternLayer.length - 1].equals(WeEventConstants.WILD_CARD_ALL_LAYER)) {
                log.error("'#' must be in last layer");
                return false;
            }

            // skip last layer '#'
            for (int idx = 0; idx < patternLayer.length - 1; idx++) {
                // the layer before '#' must be match
                if (!patternLayer[idx].equals(topicLayer[idx])) {
                    return false;
                }
            }
            return true;
        } else {
            log.error("no wildcard character in pattern");
            return false;
        }
    }
}
