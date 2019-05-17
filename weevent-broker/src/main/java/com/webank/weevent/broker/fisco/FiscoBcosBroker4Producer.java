package com.webank.weevent.broker.fisco;

import java.nio.charset.StandardCharsets;

import com.webank.weevent.broker.fisco.constant.WeEventConstants;
import com.webank.weevent.broker.fisco.util.ParamCheckUtils;
import com.webank.weevent.broker.plugin.IProducer;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.SendResult;
import com.webank.weevent.sdk.WeEvent;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FiscoBcosBroker4Producer extends FiscoBcosTopicAdmin implements IProducer {
    public FiscoBcosBroker4Producer() throws BrokerException {
        super();
    }

    @Override
    public boolean startProducer() throws BrokerException {
        return true;
    }

    @Override
    public boolean shutdownProducer() {
        return true;
    }

    @Override
    public SendResult publish(WeEvent event, Long groupId) throws BrokerException {
        log.debug("publish input param WeEvent: {}", event);

        ParamCheckUtils.validateEvent(event);

        SendResult sendResult = this.fiscoBcosDelegate.publishEvent(event.getTopic(), groupId, new String(event.getContent(), StandardCharsets.UTF_8), JSON.toJSONString(event.getExtensions()));
        log.info("publish success: {}", sendResult);
        return sendResult;
    }

    @Override
    public void publish(WeEvent event, Long groupId, SendCallBack callBack) throws BrokerException {
        log.debug("publish input param WeEvent: {}", event);

        ParamCheckUtils.validateEvent(event);
        ParamCheckUtils.validateSendCallBackNotNull(callBack);

        log.debug("publish with callback input param WeEvent: {}", event);
        this.fiscoBcosDelegate.publishEvent(event.getTopic(), groupId, new String(event.getContent(), StandardCharsets.UTF_8), JSON.toJSONString(event.getExtensions()), callBack);
    }
}
