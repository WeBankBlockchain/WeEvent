package com.webank.weevent.broker.plugin;

import java.util.List;

import com.webank.weevent.broker.fisco.dto.ListPage;
import com.webank.weevent.protocol.rest.entity.GroupGeneral;
import com.webank.weevent.protocol.rest.entity.QueryEntity;
import com.webank.weevent.protocol.rest.entity.TbBlock;
import com.webank.weevent.protocol.rest.entity.TbNode;
import com.webank.weevent.protocol.rest.entity.TbTransHash;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.TopicInfo;
import com.webank.weevent.sdk.TopicPage;
import com.webank.weevent.sdk.WeEvent;

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
     * @return boolean true is success
     * @throws BrokerException BrokerException
     */
    boolean open(String topic, String groupId) throws BrokerException;

    /**
     * Close a topic in clock chain, can not pub/sub event again after this.
     *
     * @param topic the topic
     * @return boolean true if success
     * @throws BrokerException BrokerException
     */
    boolean close(String topic, String groupId) throws BrokerException;

    /**
     * get WeEvent by eventId
     *
     * @param eventId the eventId
     * @return boolean true if success
     * @throws BrokerException BrokerException
     */
    WeEvent getEvent(String eventId, String groupId) throws BrokerException;


    /**
     * Is topic exist or not.
     *
     * @param topic the topic
     * @return boolean true if exist
     * @throws BrokerException BrokerException
     */
    boolean exist(String topic, String groupId) throws BrokerException;

    /**
     * List all topic in system.
     *
     * @param pageIndex the page index, index first page from 0
     * @param pageSize the page size
     * @return TopicPage page list
     * @throws BrokerException BrokerException
     */
    TopicPage list(Integer pageIndex, Integer pageSize, String groupId) throws BrokerException;

    /**
     * Get one topic state.
     *
     * @param topic the topic
     * @return TopicInfo topic info
     * @throws BrokerException BrokerException
     */
    TopicInfo state(String topic, String groupId) throws BrokerException;

    /**
     * list group id in system
     *
     * @return group id
     * @throws BrokerException BrokerException
     */
    List<String> listGroupId() throws BrokerException;

    GroupGeneral getGroupGeneral(String groupId) throws BrokerException;

    ListPage<TbTransHash> queryTransList(QueryEntity queryEntity) throws BrokerException;

    ListPage<TbBlock> queryBlockList(QueryEntity queryEntity) throws BrokerException;

    ListPage<TbNode> queryNodeList(QueryEntity queryEntity) throws BrokerException;


}
