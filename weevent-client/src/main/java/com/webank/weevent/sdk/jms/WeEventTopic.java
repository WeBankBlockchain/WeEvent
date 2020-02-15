package com.webank.weevent.sdk.jms;


import javax.jms.JMSException;
import javax.jms.Topic;

import lombok.Data;

/**
 * WeEvent JMS Topic.
 *
 * @author matthewliu
 * @since 2019/03/25
 */
@Data
public class WeEventTopic implements Topic {
    private final String topicName;

    private String offset;
    private String groupId;
    private String continueSubscriptionId;
    private boolean isFile;

    public WeEventTopic(String topicName) {
        this.topicName = topicName;
    }

    @Override
    public String getTopicName() throws JMSException {
        return this.topicName;
    }
}
