package com.webank.weevent.broker.fisco;

import java.nio.charset.StandardCharsets;
import java.util.List;

import com.webank.weevent.broker.fisco.util.ParamCheckUtils;
import com.webank.weevent.broker.plugin.IProducer;
import com.webank.weevent.protocol.rest.entity.GroupGeneral;
import com.webank.weevent.protocol.rest.entity.QueryEntity;
import com.webank.weevent.protocol.rest.entity.TbBlock;
import com.webank.weevent.protocol.rest.entity.TbTransHash;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.SendResult;
import com.webank.weevent.sdk.WeEvent;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FiscoBcosBroker4Producer extends FiscoBcosTopicAdmin implements IProducer {

    @Override
    public boolean startProducer() {
        return true;
    }

    @Override
    public boolean shutdownProducer() {
        return true;
    }

    @Override
    public SendResult publish(WeEvent event, String groupId) throws BrokerException {
        log.debug("publish {} groupId: {}", event, groupId);

        ParamCheckUtils.validateEvent(event);
        this.validateGroupId(groupId);

        // publishEvent support async operator in callback
        SendResult sendResult = fiscoBcosDelegate.publishEvent(event.getTopic(),
                Long.parseLong(groupId),
                new String(event.getContent(), StandardCharsets.UTF_8),
                JSON.toJSONString(event.getExtensions()));

        log.info("publish result: {}", sendResult);
        return sendResult;
    }

    @Override
    public GroupGeneral getGroupGeneral(String groupId) throws BrokerException {
        return fiscoBcosDelegate.getGroupGeneral(groupId);
    }

    @Override
    public List<TbTransHash> queryTransList(QueryEntity queryEntity) throws BrokerException {
        return fiscoBcosDelegate.queryTransList(queryEntity);
    }

    @Override
    public List<TbBlock> queryBlockList(QueryEntity queryEntity) throws BrokerException {
        return fiscoBcosDelegate.queryBlockList(queryEntity);
    }
}
