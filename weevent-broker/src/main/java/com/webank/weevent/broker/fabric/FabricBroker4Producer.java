package com.webank.weevent.broker.fabric;

import com.webank.weevent.broker.fisco.util.ParamCheckUtils;
import com.webank.weevent.broker.plugin.IProducer;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.SendResult;
import com.webank.weevent.sdk.WeEvent;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FabricBroker4Producer extends FabricTopicAdmin implements IProducer {
    @Override
    public boolean startProducer() throws BrokerException {
        return true;
    }

    @Override
    public boolean shutdownProducer() {
        return true;
    }

    @Override
    public SendResult publish(WeEvent event, String groupId) throws BrokerException {
        log.debug("publish input param WeEvent: {}", event);
        ParamCheckUtils.validateEvent(event);

        return null;
    }
}
