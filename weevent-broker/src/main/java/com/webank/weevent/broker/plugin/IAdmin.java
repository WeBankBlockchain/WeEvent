package com.webank.weevent.broker.plugin;


import java.util.List;

import com.webank.weevent.protocol.rest.entity.GroupGeneral;
import com.webank.weevent.protocol.rest.entity.QueryEntity;
import com.webank.weevent.protocol.rest.entity.TbBlock;
import com.webank.weevent.protocol.rest.entity.TbNode;
import com.webank.weevent.protocol.rest.entity.TbTransHash;
import com.webank.weevent.sdk.BrokerException;

/**
 * Base interface for event admin.
 * <p>
 *
 *
 * @author puremilkfan
 * @since 2019/09/18
 */
public interface IAdmin extends IEventTopic {

    static IAdmin build() {
        return build("fisco");
    }

    /**
     * Factory method, build a IAdmin run in agent model.
     * <p>
     * Please setup a event agent first with tools @see.
     *
     * @param blockChain "fisco" or "fabric"
     * @return IProducer
     */
    static IAdmin build(String blockChain) {
        // Use reflect to decouple block chain implement.
        try {
            switch (blockChain) {
                case "fisco":
                    Class<?> fisco = Class.forName("com.webank.weevent.broker.fisco.FiscoBcosBroker4Admin");
                    return (IAdmin) fisco.newInstance();

                case "fabric":
                default:
                    return null;
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            return null;
        }
    }

    GroupGeneral getGroupGeneral(String groupId) throws BrokerException;

    List<TbTransHash> queryTransList(QueryEntity queryEntity) throws BrokerException;

    List<TbBlock> queryBlockList(QueryEntity queryEntity) throws BrokerException;

    List<TbNode> queryNodeList(QueryEntity queryEntity) throws BrokerException;
}
