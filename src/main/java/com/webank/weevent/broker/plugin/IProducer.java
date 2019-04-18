package com.webank.weevent.broker.plugin;


import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.SendResult;
import com.webank.weevent.sdk.WeEvent;

/**
 * Base interface for event producer.
 * <p>
 * It is a sample of publish event:
 * //@formatter:off
 * <pre>
 * import com.webank.weevent.sdk.SendResult;
 * import com.webank.weevent.sdk.BrokerException;
 * import com.webank.weevent.broker.plugin.IProducer;
 * import com.webank.weevent.sdk.WeEvent;
 * try {
 *     // get handler
 *     IProducer producer = IProducer.build();
 *     // make sure topic exist
 *     producer.open("my topic name");
 *     // start channel
 *     producer.startProducer();
 *     // publish 10 events in synchronous
 *     for (int i = 0; i &lt; 10; i++) {
 *         // send event
 *         SendResult sendResult = producer.publish(new WeEvent("my topic name", "hello world.".getBytes()));
 *         // send result
 *         System.out.println(sendResult.getStatus());
 *         System.out.println(sendResult.getEventId());
 *     }
 *     // stop channel
 *     producer.shutdownProducer();
 * } catch (BrokerException e) {
 *     e.printStackTrace();
 * }
 * </pre>
 * //@formatter:on
 *
 * @author matthewliu
 * @since 2018/11/02
 */
public interface IProducer extends IEventTopic {

    static IProducer build() {
        return build("fisco");
    }

    /**
     * Factory method, build a IProducer run in agent model.
     * <p>
     * Please setup a event agent first with tools @see.
     *
     * @param blockChain "fisco" or "fabric"
     * @return IProducer
     */
    static IProducer build(String blockChain) {
        // Use reflect to decouple block chain implement.
        try {
            switch (blockChain) {
                case "fisco":
                    Class<?> fisco = Class.forName("com.webank.weevent.broker.fisco.FiscoBcosBroker4Producer");
                    return (IProducer) fisco.newInstance();

                case "fabric":
                default:
                    return null;
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            return null;
        }
    }

    /**
     * Start a producer channel.
     *
     * @return boolean true if success
     * @throws BrokerException BrokerException
     */
    boolean startProducer() throws BrokerException;

    /**
     * Shutdown a producer channel.
     *
     * @return boolean true if success
     */
    boolean shutdownProducer();

    /**
     * Publish a event in synchronous way.
     *
     * @param event the event
     * @return SendResult SendResult
     * @throws BrokerException BrokerException
     */
    SendResult publish(WeEvent event) throws BrokerException;

    /*
     * TODO
     * publishBatch(List<WeEvent> event) is a good idea.
     */

    /**
     * Interface used by {@link IProducer#publish(WeEvent, SendCallBack)}
     */
    interface SendCallBack {
        /**
         * Called while publish is complete.
         *
         * @param sendResult the sendResult
         */
        void onComplete(SendResult sendResult);

        /**
         * Called while raise exception in publish.
         *
         * @param e the e
         */
        void onException(Throwable e);
    }

    /**
     * Publish a event in asynchronous way.
     *
     * @param event the event
     * @param callBack the callBack {@link SendCallBack}
     * @throws BrokerException BrokerException
     */
    void publish(WeEvent event, SendCallBack callBack) throws BrokerException;
}
