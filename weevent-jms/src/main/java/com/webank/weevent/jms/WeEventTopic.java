package com.webank.weevent.jms;


import java.util.Map;

import javax.jms.Topic;

/**
 * WeEvent JMS Topic.
 *
 * @author matthewliu
 * @since 2019/03/25
 */
public class WeEventTopic implements Topic {
    private final String topicName;

    private String offset;
    private String groupId;
    private Map<String, String> extension;
    private boolean isFile;

    public WeEventTopic(String topicName) {
        this.topicName = topicName;
    }

    @Override
    public String getTopicName() {
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

    public Map<String, String> getExtension() {
        return this.extension;
    }

    public void setExtension(Map<String, String> extension) {
        this.extension = extension;
    }

    public boolean isFile() {
        return this.isFile;
    }

    public void setFile(boolean file) {
        this.isFile = file;
    }

}
