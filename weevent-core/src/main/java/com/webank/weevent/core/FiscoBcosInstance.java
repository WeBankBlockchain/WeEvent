package com.webank.weevent.core;


import com.webank.weevent.client.BrokerException;
import com.webank.weevent.core.config.FiscoConfig;
import com.webank.weevent.core.fisco.FiscoBcosBroker4Consumer;
import com.webank.weevent.core.fisco.FiscoBcosBroker4Producer;
import com.webank.weevent.core.fisco.web3sdk.FiscoBcosDelegate;

/**
 * FISCO-BCOS instance.
 *
 * <p>
 * It is a sample of subscribe event:
 * //@formatter:off
 * <pre>
 * import com.webank.weevent.core.FiscoBcos;
 * import com.webank.weevent.core.IConsumer;
 * import com.webank.weevent.core.IProducer;
 * import com.webank.weevent.sdk.BrokerException;
 * import com.webank.weevent.sdk.WeEvent;
 *
 * try {
 *     // new FiscoBcos instance
 *     FiscoBcosInstance fiscoBcos = new FiscoBcosInstance(new FiscoConfig();
 *
 *     // new IProducer handler
 *     IProducer producer = fiscoBcos.buildProducer();
 *     producer.startProducer();
 *     producer.open("com.weevent.test");
 *     SendResult sendResult = producer.publish(new WeEvent("com.weevent.test", "hello weevent".getBytes()), "1");
 *     System.out.println(sendResult);
 *
 *     // new IConsumer handler
 *     IConsumer consumer = fiscoBcos.buildConsumer();
 *     // make sure topic exist
 *     consumer.open("com.weevent.test");
 *     // start channel
 * 	   consumer.startConsumer();
 *     String subscriptionId = consumer.subscribe("com.weevent.test", WeEvent.OFFSET_LAST, new IConsumer.ConsumerListener() {
 *         public void onEvent(String subscriptionId, WeEvent event) {
 *             System.out.println(event);
 *         }
 *         public void onException(Throwable e) {
 *             System.out.println("exception");
 *         }
 *     });
 *     //unSubscribe
 *     consumer.unSubscribe(subscriptionId);
 * } catch (BrokerException e) {
 *     e.printStackTrace();
 * }
 * </pre>
 * //@formatter:on
 *
 * @author matthewliu
 * @since 2020/03/01
 */
public class FiscoBcosInstance {
    private final FiscoBcosDelegate fiscoBcosDelegate;

    public FiscoBcosInstance(FiscoConfig config) throws BrokerException {
        this.fiscoBcosDelegate = new FiscoBcosDelegate();
        this.fiscoBcosDelegate.initProxy(config);
    }

    /**
     * build a producer
     *
     * @return IProducer producer handler
     */
    public IProducer buildProducer() {
        return new FiscoBcosBroker4Producer(this.fiscoBcosDelegate);
    }

    /**
     * build a consumer
     *
     * @return IConsumer consumer handler
     */
    public IConsumer buildConsumer() {
        return new FiscoBcosBroker4Consumer(this.fiscoBcosDelegate);
    }
}
