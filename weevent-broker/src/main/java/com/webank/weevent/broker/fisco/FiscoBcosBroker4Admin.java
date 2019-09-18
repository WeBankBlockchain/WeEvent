package com.webank.weevent.broker.fisco;

import java.util.List;

import com.webank.weevent.broker.plugin.IAdmin;
import com.webank.weevent.protocol.rest.entity.GroupGeneral;
import com.webank.weevent.protocol.rest.entity.QueryEntity;
import com.webank.weevent.protocol.rest.entity.TbBlock;
import com.webank.weevent.protocol.rest.entity.TbNode;
import com.webank.weevent.protocol.rest.entity.TbTransHash;
import com.webank.weevent.sdk.BrokerException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FiscoBcosBroker4Admin extends FiscoBcosTopicAdmin implements IAdmin {

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

    @Override
    public List<TbNode> queryNodeList(QueryEntity queryEntity) throws BrokerException {
        return fiscoBcosDelegate.queryNodeList(queryEntity);
    }
}
