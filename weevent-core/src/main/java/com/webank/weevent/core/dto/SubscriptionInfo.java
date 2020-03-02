package com.webank.weevent.core.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author websterchen
 * @version 1.0
 * @since 2019/4/4
 */
@Getter
@Setter
@ToString
public class SubscriptionInfo {
    /**
     * subscribe from which protocol, restful or json rpc, etc.
     */
    private String interfaceType;

    /**
     * notified event count.
     */
    private String notifiedEventCount;

    /**
     * notifying event count.
     */
    private String notifyingEventCount;

    /**
     * event notify TimeStamp.
     */
    private String notifyTimeStamp;

    /**
     * binding topic name.
     */
    private String topicName;

    /**
     * subscription ID
     */
    private String subscribeId;

    /**
     * subscribe from which ip
     */
    private String remoteIp;

    /**
     * subscribe topic TimeStamp.
     */
    private String createTimeStamp;

    /**
     * binding groupId.
     */
    private String groupId;
}
