package com.webank.weevent.broker.fabric;

import java.nio.charset.StandardCharsets;

import com.webank.weevent.broker.fisco.util.ParamCheckUtils;
import com.webank.weevent.broker.plugin.IProducer;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.SendResult;
import com.webank.weevent.sdk.WeEvent;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
/**
 * @author websterchen
 * @version v1.1
 * @since 2019/8/10
 */
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
    public SendResult publish(WeEvent event, String channelName) throws BrokerException {
        log.debug("publish input param WeEvent: {}", event);
        ParamCheckUtils.validateEvent(event);
        this.validateChannelName(channelName);
        SendResult sendResult = fabricDelegate.publishEvent(event.getTopic(),
                channelName,
                new String(event.getContent(), StandardCharsets.UTF_8),
                JSON.toJSONString(event.getExtensions()));
        return sendResult;
    }
}
