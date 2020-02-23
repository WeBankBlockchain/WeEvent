package com.webank.weevent.broker.plugin;

import java.util.Map;

import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.WeEvent;

import lombok.NonNull;

/**
 * Base interface for event consumer.
 * <p>
 * It is a sample of subscribe event:
 * //@formatter:off
 * <pre>
 * import com.webank.weevent.sdk.BrokerException;
 * import com.webank.weevent.broker.plugin.IConsumer;
 * import com.webank.weevent.sdk.WeEvent;
 * try {
 *     // get handler
 *     IConsumer consumer = IConsumer.build();
 *     // make sure topic exist
 *     consumer.open("my_topic_name");
 *     // start channel
 * 	   consumer.startConsumer();
 *     String subscriptionId = consumer.subscribe("my_topic_name", WeEvent.OFFSET_LAST, new IConsumer.ConsumerListener() {
 *         public void onEvent(String subscriptionId, WeEvent event) {
 *             System.out.println(event);
 *         }
 *         public void onException(Throwable e) {
 *             System.out.println("exception");
 *         }
 *     });
 *     //unSubscribe
 *     consumer.unSubscribe(subscriptionId);
 * 	   // stop channel
 * 	   consumer.shutdownConsumer();
 * } catch (BrokerException e) {
 *     e.printStackTrace();
 * }
 * </pre>
 * //@formatter:on
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
        RemoteIP;
    }

    /**
     * This support single topic subscribe
     * from next event after this offset(an event id), WeEvent.OFFSET_FIRST if from head of queue, WeEvent.OFFSET_LAST if from tail of queue
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
     * @param offset, from next event after this offset(an event id), WeEvent.OFFSET_FIRST if from head of queue, WeEvent.OFFSET_LAST if from tail of queue
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
    Map<String, Object> listSubscription(String groupId) throws BrokerException;
}
