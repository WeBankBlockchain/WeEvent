package com.webank.weevent.sdk.jms;


import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Topic;

/**
 * WeEvent JMS Topic.
 *
 * @author matthewliu
 * @since 2019/03/25
 */
public class WeEventTopic implements Topic {
    private String topicName;
    private String offset;
    private String groupId;
    private Map<String, String> extensions;
    private String continueSubscriptionId;

    public WeEventTopic(String topicName) {
        this.topicName = topicName;
    }

    @Override
    public String getTopicName() throws JMSException {
        return this.topicName;
    }

    public String getOffset() {
        return this.offset;
    }

    public void setOffset(String offset) {
        this.offset = offset;
    }

    public String getGroupId() {
        return this.groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public Map<String, String> getExtensions() {
        return this.extensions;
    }

    public void setExtensions(Map<String, String> extensions) {
        this.extensions = extensions;
    }

    public String getContinueSubscriptionId() {
        return this.continueSubscriptionId;
    }

    public void setContinueSubscriptionId(String continueSubscriptionId) {
        this.continueSubscriptionId = continueSubscriptionId;
    }
}
