package com.webank.weevent.core.dto;

import java.text.SimpleDateFormat;
import java.util.Arrays;

import com.webank.weevent.core.task.Subscription;

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

    public static SubscriptionInfo fromSubscription(Subscription subscription) {
        SubscriptionInfo subscriptionInfo = new SubscriptionInfo();

        subscriptionInfo.setInterfaceType(subscription.getInterfaceType());
        subscriptionInfo.setNotifiedEventCount(subscription.getNotifiedEventCount().toString());
        subscriptionInfo.setNotifyingEventCount(subscription.getNotifyingEventCount().toString());
        subscriptionInfo.setNotifyTimeStamp(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                .format(subscription.getNotifyTimeStamp()));
        subscriptionInfo.setRemoteIp(subscription.getRemoteIp());
        subscriptionInfo.setCreateTimeStamp(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                .format(subscription.getCreateTimeStamp()));
        subscriptionInfo.setGroupId(subscription.getGroupId());

        // Arrays.toString will append plus "[]"
        if (subscription.getTopics().length == 1) {
            subscriptionInfo.setTopicName(subscription.getTopics()[0]);
        } else {
            subscriptionInfo.setTopicName(Arrays.toString(subscription.getTopics()));
        }

        subscriptionInfo.setSubscribeId(subscription.getUuid());

        return subscriptionInfo;
    }
}
