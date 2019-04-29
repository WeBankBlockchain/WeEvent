package com.webank.weevent.broker.plugin;


import java.util.Map;

import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.WeEvent;

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
 *     String subscriptionId = consumer.subscribe("my topic name", WeEvent.OFFSET_LAST, new IConsumer.ConsumerListener() {
 *         public void onEvent(List&lt;WeEvent&gt; events) {
 *             for (WeEvent event : events) {
 *                 System.out.println(event);
 *             }
 *         }
 *         public void onException(Throwable e) {
 *             System.out.println("exception");
 *         }
 *     });
 *     //unSubscribe
 *     consumer.unSubscribe(subscriptionId);
 * 	   // stop channel
 * 	   //consumer.shutdownConsumer();
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
    static IConsumer build() {
        return build("fisco");
    }

    /**
     * Factory method, build a IConsumer run in agent model.
     * <p>
     * Please setup a event agent first with tools @see.
     *
     * @param blockChain "fisco" or "fabric"
     * @return IConsumer handler
     */
    static IConsumer build(String blockChain) {
        // Use reflect to decouple block chain implement.
        try {
            switch (blockChain) {
                case "fisco":
                    Class<?> fisco = Class.forName("com.webank.weevent.broker.fisco.FiscoBcosBroker4Consumer");
                    return (IConsumer) fisco.newInstance();

                case "fabric":
                default:
                    return null;
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            return null;
        }
    }

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
    }

    /**
     * This support multiple topic subscribe
     * from next event after this offset(an event id), WeEvent.OFFSET_FIRST if from head of queue, WeEvent.OFFSET_LAST if from tail of queue
     *
     * @param topics topic list
     * @param interfaceType interface type jsonrpc or restful or stomp
     * @param listener callback
     * @return subscription Id relation to topic
     * @throws BrokerException invalid input
     */
    Map<String, String> subscribe(Map<String, String> topics, String interfaceType, ConsumerListener listener) throws BrokerException;

    /**
     * This support single topic subscribe
     *
     * @param topic topic name
     * @param offset, from next event after this offset(an event id), WeEvent.OFFSET_FIRST if from head of queue, WeEvent.OFFSET_LAST if from tail of queue
     * @param interfaceType interface type jsonrpc or restful or stomp
     * @param listener callback
     * @return subscription Id
     * @throws BrokerException invalid input param
     */
    String subscribe(String topic, String offset, String interfaceType, ConsumerListener listener) throws BrokerException;

    /**
     * This support single topic subscribe
     *
     * @param topic topic name
     * @param offset, from next event after this offset(an event id), WeEvent.OFFSET_FIRST if from head of queue, WeEvent.OFFSET_LAST if from tail of queue
     * @param subscriptionId Continue the client last time listening
     * @param interfaceType interface type jsonrpc or restful or stomp
     * @param listener callback
     * @return subscription Id
     * @throws BrokerException invalid input param
     */
    String subscribe(String topic, String offset, String subscriptionId, String interfaceType, ConsumerListener listener) throws BrokerException;

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
     * Whether the Consumer has started
     *
     * @return boolean true if started already
     */
    boolean isStarted();

    /**
     * it will start the consumer process so that the consumer is ready to receive the events once it subscribe topic.
     *
     * @return success if true
     * @throws BrokerException invalid input param
     */
    boolean startConsumer() throws BrokerException;

    /**
     * Shutdown a consumer channel.
     * <p>
     * Stop listening and loop thread etc.
     *
     * @return boolean true if success
     */
    boolean shutdownConsumer();

    /**
     * get local listen subscribeid list
     *
     * @return boolean true if success
     */
    Map<String, Object> getInnerSubscription();
}
