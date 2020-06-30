package com.webank.weevent.jms;


import java.util.Map;

import javax.jms.Topic;

import lombok.Getter;
import lombok.Setter;

/**
 * WeEvent JMS Topic.
 *
 * @author matthewliu
 * @since 2019/03/25
 */
@Getter
@Setter
public class WeEventTopic implements Topic {
    private final String topicName;

    private String offset;
    private String groupId;
    private Map<String, String> extension;

    public WeEventTopic(String topicName) {
        this.topicName = topicName;
    }

}
