package com.webank.weevent.core;

import java.util.List;

import com.webank.weevent.client.BrokerException;
import com.webank.weevent.client.TopicInfo;
import com.webank.weevent.client.TopicPage;
import com.webank.weevent.client.WeEvent;
import com.webank.weevent.core.dto.ContractContext;
import com.webank.weevent.core.dto.GroupGeneral;
import com.webank.weevent.core.dto.ListPage;
import com.webank.weevent.core.dto.QueryEntity;
import com.webank.weevent.core.dto.TbBlock;
import com.webank.weevent.core.dto.TbNode;
import com.webank.weevent.core.dto.TbTransHash;

/**
 * Base interface for topic admin.
 *
 * @author matthewliu
 * @since 2018/11/02
 */
public interface IEventTopic {
    /**
     * Open a topic in block chain, create it if not exist.
     *
     * @param topic the topic
     * @param groupId group id
     * @return boolean true is success
     * @throws BrokerException BrokerException
     */
    boolean open(String topic, String groupId) throws BrokerException;

    /**
     * Close a topic in clock chain, can not pub/sub event again after this.
     *
     * @param topic the topic
     * @param groupId group id
     * @return boolean true if success
     * @throws BrokerException BrokerException
     */
    boolean close(String topic, String groupId) throws BrokerException;

    /**
     * get WeEvent by eventId
     *
     * @param eventId the eventId
     * @param groupId group id
     * @return boolean true if success
     * @throws BrokerException BrokerException
     */
    WeEvent getEvent(String eventId, String groupId) throws BrokerException;


    /**
     * Is topic exist or not.
     *
     * @param topic the topic
     * @param groupId group id
     * @return boolean true if exist
     * @throws BrokerException BrokerException
     */
    boolean exist(String topic, String groupId) throws BrokerException;

    /**
     * List all topic in system.
     *
     * @param pageIndex the page index, index first page from 0
     * @param pageSize the page size
     * @param groupId group id
     * @return TopicPage page list
     * @throws BrokerException BrokerException
     */
    TopicPage list(Integer pageIndex, Integer pageSize, String groupId) throws BrokerException;

    /**
     * Get one topic state.
     *
     * @param topic the topic
     * @param groupId group id
     * @return TopicInfo topic info
     * @throws BrokerException BrokerException
     */
    TopicInfo state(String topic, String groupId) throws BrokerException;

    /**
     * get block height
     *
     * @param groupId group id
     * @return block height
     * @throws BrokerException BrokerException
     */
    Long getBlockHeight(String groupId) throws BrokerException;

    /**
     * list group id in system
     *
     * @return group id
     * @throws BrokerException BrokerException
     */
    List<String> listGroupId() throws BrokerException;

    // advanced admin in the followings
    GroupGeneral getGroupGeneral(String groupId) throws BrokerException;

    ListPage<TbTransHash> queryTransList(QueryEntity queryEntity) throws BrokerException;

    ListPage<TbBlock> queryBlockList(QueryEntity queryEntity) throws BrokerException;

    ListPage<TbNode> queryNodeList(QueryEntity queryEntity) throws BrokerException;

    ContractContext getContractContext(String groupId) throws BrokerException;

    boolean addOperator(String groupId, String topicName, String operatorAddress) throws BrokerException;

    boolean delOperator(String groupId, String topicName, String operatorAddress) throws BrokerException;

    List<String> listOperator(String groupId, String topicName) throws BrokerException;

    void validateGroupId(String groupId) throws BrokerException;
}
