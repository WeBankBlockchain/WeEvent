package com.webank.weevent.client.stomp;


import lombok.Getter;
import lombok.Setter;

/**
 * WeEvent Topic.
 *
 * @author matthewliu
 * @since 2019/03/25
 */
@Getter
@Setter
public class WeEventTopic {
    private final String topicName;

    private String offset;
    private String groupId;
    private String continueSubscriptionId;
    private boolean isFile;

    public WeEventTopic(String topicName) {
        this.topicName = topicName;
    }

}
