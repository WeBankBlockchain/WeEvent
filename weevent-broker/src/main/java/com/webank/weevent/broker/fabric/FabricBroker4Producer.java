package com.webank.weevent.broker.fabric;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

import com.webank.weevent.broker.fabric.sdk.FabricDelegate;
import com.webank.weevent.broker.fisco.util.DataTypeUtils;
import com.webank.weevent.broker.fisco.util.ParamCheckUtils;
import com.webank.weevent.broker.plugin.IProducer;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.SendResult;
import com.webank.weevent.sdk.WeEvent;

import lombok.extern.slf4j.Slf4j;

/**
 * @author websterchen
 * @version v1.1
 * @since 2019/8/10
 */
@Slf4j
public class FabricBroker4Producer extends FabricTopicAdmin implements IProducer {
    public FabricBroker4Producer(FabricDelegate fabricDelegate) {
        super(fabricDelegate);
    }

    @Override
    public boolean startProducer() {
        return true;
    }

    @Override
    public boolean shutdownProducer() {
        return true;
    }

    @Override
    public CompletableFuture<SendResult> publish(WeEvent event, String channelName) throws BrokerException {
        log.debug("publish input param WeEvent: {}", event);

        ParamCheckUtils.validateEvent(event);
        this.validateChannelName(channelName);
        return fabricDelegate.publishEvent(event.getTopic(),
                channelName,
                new String(event.getContent(), StandardCharsets.UTF_8),
                DataTypeUtils.object2Json(event.getExtensions()));
    }
}
