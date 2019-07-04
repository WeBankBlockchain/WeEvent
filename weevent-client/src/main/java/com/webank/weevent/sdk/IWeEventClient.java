package com.webank.weevent.sdk;

import java.util.Map;


public interface IWeEventClient {
    /**
     * Get the client handler of WeEvent's broker with default url, http://localhost:8080/weevent.
     *
     * @throws BrokerException broker exception
     */
    static IWeEventClient build() throws BrokerException {
        return new WeEventClient();
    }

    /**
     * Get the client handler of WeEvent's broker with custom url.
     *
     * @param brokerUrl WeEvent's broker url, like http://localhost:8080/weevent
     * @throws BrokerException broker exception
     */
    static IWeEventClient build(String brokerUrl) throws BrokerException {
        return new WeEventClient(brokerUrl);
    }

    /**
     * Get the client handler of WeEvent's broker custom url and account authorization.
     *
     * @param brokerUrl WeEvent's broker url, like http://localhost:8080/weevent
     * @param userName account name
     * @param password password
     * @throws BrokerException broker exception
     */
    static IWeEventClient build(String brokerUrl, String userName, String password) throws BrokerException {
        return new WeEventClient(brokerUrl, userName, password);
    }

    /**
     * Publish an event to topic.
     *
     * @param topic topic name
     * @param content topic data
     * @return send result, SendResult.SUCCESS if success, and SendResult.eventId
     * @throws BrokerException broker exception
     */
    SendResult publish(String topic, byte[] content) throws BrokerException;

    /**
     * Subscribe events from topic.
     *
     * @param topic topic name
     * @param offset, from next event after this offset(an event id), WeEvent.OFFSET_FIRST if from head of queue, WeEvent.OFFSET_LAST if from tail of queue
     * @param listener callback
     * @return subscription Id
     * @throws BrokerException invalid input param
     */
    String subscribe(String topic, String offset, WeEventClient.EventListener listener) throws BrokerException;

    /**
     * Unsubscribe an exist subscription subscribed by subscribe interface.
     * The consumer will no longer receive messages from broker after this.
     *
     * @param subscriptionId invalid input
     * @return success if true
     * @throws BrokerException broker exception
     */

    boolean unSubscribe(String subscriptionId) throws BrokerException;


    /**
     * Open a topic
     *
     * @param topic topic name
     * @return true if success
     * @throws BrokerException broker exception
     */
    boolean open(String topic) throws BrokerException;

    /**
     * Close a topic.
     *
     * @param topic topic name
     * @return true if success
     * @throws BrokerException broker exception
     */
    boolean close(String topic) throws BrokerException;

    /**
     * Check a topic is exist or not.
     *
     * @param topic topic name
     * @return true if exist
     * @throws BrokerException broker exception
     */
    boolean exist(String topic) throws BrokerException;

    /**
     * List all topics in WeEvent's broker.
     *
     * @param pageIndex page index, from 0
     * @param pageSize page size, [10, 100)
     * @return topic list
     * @throws BrokerException broker exception
     */
    TopicPage list(Integer pageIndex, Integer pageSize) throws BrokerException;

    /**
     * Get a topic information.
     *
     * @param topic topic name
     * @return topic information
     * @throws BrokerException broker exception
     */
    TopicInfo state(String topic) throws BrokerException;

    /**
     * Get an event information.
     *
     * @param eventId event id
     * @return WeEvent
     * @throws BrokerException broker exception
     */
    WeEvent getEvent(String eventId) throws BrokerException;

    /**
     * Publish an event to topic.
     *
     * @param topic topic name
     * @param content topic data
     * @return send result, SendResult.SUCCESS if success, and SendResult.eventId
     * @throws BrokerException broker exception
     */
    SendResult publish(String topic, String groupId, byte[] content, Map<String, String> extensions) throws BrokerException;

    /**
     * Subscribe events from topic.
     *
     * @param topic topic name
     * @param offset, from next event after this offset(an event id), WeEvent.OFFSET_FIRST if from head of queue, WeEvent.OFFSET_LAST if from tail of queue
     * @param listener callback
     * @return subscription Id
     * @throws BrokerException invalid input param
     */
    String subscribe(String topic, String groupId, String offset, WeEventClient.EventListener listener) throws BrokerException;

    /**
     * Subscribe events from topic.
     *
     * @param topic topic name
     * @param offset, from next event after this offset(an event id), WeEvent.OFFSET_FIRST if from head of queue, WeEvent.OFFSET_LAST if from tail of queue
     * @param listener callback
     * @return subscription Id
     * @throws BrokerException invalid input param
     */
<<<<<<< HEAD
    String subscribe(String topic, String groupId, String offset,String contnueSubScriptionId, WeEventClient.EventListener listener) throws BrokerException;
=======
    String subscribe(String topic, String groupId, String offset, String contnueSubScriptionId, WeEventClient.EventListener listener) throws BrokerException;
>>>>>>> upstream/master

    /**
     * Publish an event to topic.
     *
     * @param topic topic name
     * @param content topic data
     * @return send result, SendResult.SUCCESS if success, and SendResult.eventId
     * @throws BrokerException broker exception
     */
    SendResult publish(String topic, byte[] content, Map<String, String> extensions) throws BrokerException;

    /**
     * Close a topic.
     *
     * @param topic topic name
     * @param groupId which group to close
     * @return true if success
     * @throws BrokerException broker exception
     */
    boolean close(String topic, String groupId) throws BrokerException;

    /**
     * Check a topic is exist or not.
     *
     * @param topic topic name
     * @param groupId which group to exit
     * @return true if exist
     * @throws BrokerException broker exception
     */
    boolean exist(String topic, String groupId) throws BrokerException;

    /**
     * Open a topic.
     *
     * @param topic topic name
     * @param groupId which group to open
     * @return true if success
     * @throws BrokerException broker exception
     */
    boolean open(String topic, String groupId) throws BrokerException;

    /**
     * List all topics in WeEvent's broker.
     *
     * @param pageIndex page index, from 0
     * @param pageSize page size, [10, 100)
     * @return topic list
     * @throws BrokerException broker exception
     */
    TopicPage list(Integer pageIndex, Integer pageSize, String groupId) throws BrokerException;

    /**
     * Get a topic information.
     *
     * @param topic topic name
     * @return topic information
     * @throws BrokerException broker exception
     */
    TopicInfo state(String topic, String groupId) throws BrokerException;

    /**
     * Get an event information.
     *
     * @param eventId event id
     * @return WeEvent
     * @throws BrokerException broker exception
     */
    WeEvent getEvent(String eventId, String groupId) throws BrokerException;

}
