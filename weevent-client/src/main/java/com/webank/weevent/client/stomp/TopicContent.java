package com.webank.weevent.client.stomp;


import java.util.Map;

import lombok.Getter;
import lombok.Setter;

/**
 * WeEvent Topic Content.
 *
 * @author matthewliu
 * @since 2019/03/25
 */
@Getter
@Setter
public class TopicContent {
    private final String topicName;

    private String offset;
    private String groupId;
    private Map<String, String> extension;
    private boolean isFile;

    public TopicContent(String topicName) {
        this.topicName = topicName;
    }

}
