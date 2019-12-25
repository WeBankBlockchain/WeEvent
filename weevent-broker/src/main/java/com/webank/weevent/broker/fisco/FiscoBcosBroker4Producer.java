package com.webank.weevent.broker.fisco;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

import com.webank.weevent.broker.fisco.util.DataTypeUtils;
import com.webank.weevent.broker.fisco.util.ParamCheckUtils;
import com.webank.weevent.broker.fisco.web3sdk.FiscoBcosDelegate;
import com.webank.weevent.broker.plugin.IProducer;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.SendResult;
import com.webank.weevent.sdk.WeEvent;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FiscoBcosBroker4Producer extends FiscoBcosTopicAdmin implements IProducer {

    public FiscoBcosBroker4Producer(FiscoBcosDelegate fiscoBcosDelegate){
        super(fiscoBcosDelegate);
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
    public CompletableFuture<SendResult> publish(WeEvent event, String groupIdStr) throws BrokerException {
        log.debug("publish {} groupId: {}", event, groupIdStr);

        String groupId = selectGroupId(groupIdStr);
        this.validateGroupId(groupId);
        ParamCheckUtils.validateEvent(event);

        // publishEvent support async operator in callback
        return fiscoBcosDelegate.publishEvent(event.getTopic(),
                Long.parseLong(groupId),
                new String(event.getContent(), StandardCharsets.UTF_8),
                DataTypeUtils.object2Json(event.getExtensions()));
    }
}
