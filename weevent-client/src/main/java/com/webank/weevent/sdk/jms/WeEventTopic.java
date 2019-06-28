package com.webank.weevent.sdk.jms;


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
    private String continueSubscriptionId;

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public WeEventTopic(String topicName) {
        this.topicName = topicName;
    }

    public String getOffset() {
        return offset;
    }

    public void setOffset(String offset) {
        this.offset = offset;
    }

    public String getContinueSubscriptionId() {
        return continueSubscriptionId;
    }

    public void setContinueSubscriptionId(String continueSubscriptionId) {
        this.continueSubscriptionId = continueSubscriptionId;
    }

    @Override
    public String getTopicName() throws JMSException {
        return this.topicName;
    }
}
