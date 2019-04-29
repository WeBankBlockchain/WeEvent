package com.webank.weevent.broker.plugin;

import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.TopicInfo;
import com.webank.weevent.sdk.TopicPage;
import com.webank.weevent.sdk.WeEvent;

/**
 * Base interface for topic admin.
 * <p>
 * Always create a topic first use {@link IEventTopic#open(String)}, and then pub/sub event binding on it.
 * <p>
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
    boolean open(String topic) throws BrokerException;

    /**
     * Close a topic in clock chain, can not pub/sub event again after this.
     *
     * @param topic the topic
     * @return boolean true if success
     * @throws BrokerException BrokerException
     */
    boolean close(String topic) throws BrokerException;

    /**
     * get WeEvent by eventId
     *
     * @param eventId the eventId
     * @return boolean true if success
     * @throws BrokerException BrokerException
     */
    WeEvent getEvent(String eventId) throws BrokerException;


    /**
     * Is topic exist or not.
     *
     * @param topic the topic
     * @return boolean true if exist
     * @throws BrokerException BrokerException
     */
    boolean exist(String topic) throws BrokerException;

    /**
     * List all topic in system.
     *
     * @param pageIndex the page index, index first page from 0
     * @param pageSize the page size
     * @return TopicPage page list
     * @throws BrokerException BrokerException
     */
    TopicPage list(Integer pageIndex, Integer pageSize) throws BrokerException;

    /**
     * Get one topic state.
     *
     * @param topic the topic
     * @return TopicInfo topic info
     * @throws BrokerException BrokerException
     */
    TopicInfo state(String topic) throws BrokerException;
}
