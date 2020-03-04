package com.webank.weevent.core.task;


import java.util.List;

import com.webank.weevent.client.BrokerException;
import com.webank.weevent.client.WeEvent;

/**
 * Block chain interface used by subscribe.
 *
 * @author matthewliu
 * @since 2019/08/30
 */
public interface IBlockChain {
    /**
     * idle time for every loop
     *
     * @return time in ms
     */
    int getIdleTime();

    /**
     * current block height
     *
     * @param groupId group id
     * @return block height
     * @throws BrokerException BrokerException
     */
    Long getBlockHeight(String groupId) throws BrokerException;

    /**
     * if this block chain support new block event notify
     * see FiscoBcosDelegate.IBlockEventListener
     *
     * @return true if support
     */
    boolean hasBlockEventNotify();

    /**
     * get data from block chain and it's cache
     *
     * @param blockNum block height
     * @param groupId group id
     * @return list of WeEvent
     * @throws BrokerException BrokerException
     */
    List<WeEvent> loop(Long blockNum, String groupId) throws BrokerException;
}
