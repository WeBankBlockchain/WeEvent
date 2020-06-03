package com.webank.weevent.core;

import java.util.Map;

import com.webank.weevent.client.BrokerException;
import com.webank.weevent.client.WeEvent;
import com.webank.weevent.core.dto.SubscriptionInfo;

import lombok.NonNull;

/**
 * Base interface for event consumer.
 *
 * @author matthewliu
 * @since 2018/11/02
 */
public interface IConsumer extends IEventTopic {
    /**
     * it will start the consumer process so that the consumer is ready to receive the events once it subscribe topic.
     *
     * @return success if true
     * @throws BrokerException invalid input param
     */
    boolean startConsumer() throws BrokerException;

    /**
     * Whether the Consumer has started
     *
     * @return boolean true if started already
     */
    boolean isStarted();

    /**
     * Shutdown a consumer channel.
     * Stop listening and loop thread etc.
     *
     * @return boolean true if success
     */
    boolean shutdownConsumer();

    /**
     * Interface for callback
     */
    interface ConsumerListener {
        /**
         * Called while new event arrived.
         *
         * @param subscriptionId binding which subscription
         * @param event the event
         */
        void onEvent(String subscriptionId, WeEvent event);

        /**
         * Called while raise exception.
         *
         * @param e the e
         */
        void onException(Throwable e);

        /**
         * Called while subscription closed, like unSubscribe.
         *
         * @param subscriptionId binding which subscription
         */
        default void onClose(String subscriptionId) {
            // Called while subscription closed, like unSubscribe.
        }
    }

    /**
     * defined key used in the param 'subscribe#ext'
     */
    enum SubscribeExt {
        // persist subscription Id
        SubscriptionId,
        // weevent-tag
        TopicTag,
        // from which protocol
        InterfaceType,
        // remote client ip
        RemoteIP,
        // Ephemeral notify
        Ephemeral,
    }

    /**
     * This support single topic subscribe
     * offset details:
     * WeEvent.OFFSET_FIRST, get event from head of queue
     * WeEvent.OFFSET_LAST, get event from tail of queue
     * eventID/blockHeight, get event after the eventID or blockHeight
     *
     * @param topic topic name
     * @param groupId groupId
     * @param offset offset
     * @param ext extension params in this invoke, see SubscribeExt
     * @param listener callback
     * @return subscription Id
     * @throws BrokerException invalid input
     */
    String subscribe(String topic, String groupId, String offset,
                     @NonNull Map<SubscribeExt, String> ext,
                     @NonNull ConsumerListener listener) throws BrokerException;

    /**
     * This support multiple topic subscribe
     *
     * @param topics topic list
     * @param groupId groupId
     * @param offset offset
     * @param ext extension params in this invoke, see SubscribeExt
     * @param listener callback
     * @return subscription Id
     * @throws BrokerException invalid input param
     */
    String subscribe(String[] topics, String groupId, String offset,
                     @NonNull Map<SubscribeExt, String> ext,
                     @NonNull ConsumerListener listener) throws BrokerException;

    /**
     * unsubscribe an exist subscription subscribed by subscribe interface.
     * so that the consumer will no longer receive messages from that subscription.
     *
     * @param subscriptionId invalid input
     * @return success if true
     * @throws BrokerException invalid input param
     * @throws BrokerException broker exception
     */
    boolean unSubscribe(String subscriptionId) throws BrokerException;

    /**
     * get subscription list
     *
     * @param groupId groupId
     * @return subscription list if success
     * @throws BrokerException invalid input param
     */
    Map<String, SubscriptionInfo> listSubscription(String groupId) throws BrokerException;
}
