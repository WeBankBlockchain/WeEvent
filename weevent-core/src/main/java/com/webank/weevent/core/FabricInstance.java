package com.webank.weevent.core;


import com.webank.weevent.core.config.FabricConfig;
import com.webank.weevent.core.fabric.FabricBroker4Consumer;
import com.webank.weevent.core.fabric.FabricBroker4Producer;
import com.webank.weevent.core.fabric.sdk.FabricDelegate;
import com.webank.weevent.client.BrokerException;

/**
 * Fabric instance.
 *
 * @author matthewliu
 * @since 2020/03/01
 */
public class FabricInstance {
    private FabricDelegate fabricDelegate;

    public FabricInstance(FabricConfig fabricConfig) throws BrokerException {
        this.fabricDelegate = new FabricDelegate();
        this.fabricDelegate.initProxy(fabricConfig);
    }

    /**
     * build a producer
     *
     * @return IProducer producer handler
     */
    public IProducer buildProducer() {
        return new FabricBroker4Producer(this.fabricDelegate);
    }

    /**
     * build a consumer
     *
     * @return IConsumer consumer handler
     */
    public IConsumer buildConsumer() {
        return new FabricBroker4Consumer(this.fabricDelegate);
    }
}
