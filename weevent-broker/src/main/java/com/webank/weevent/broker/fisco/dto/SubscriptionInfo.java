package com.webank.weevent.broker.fisco.dto;

import lombok.Data;

/**
 * @author websterchen
 * @version 1.0
 * @since 2019/4/4
 */
@Data
public class SubscriptionInfo {
    private String interfaceType;
    private String notifiedEventCount;
    private String notifyingEventCount;
    private String notifyTimeStamp;
    private String topicName;
    private String subscribeId;
}
