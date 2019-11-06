package com.webank.weevent.sdk;

import lombok.NonNull;

/**
 * Java Client for WeEvent
 *
 * @author crisiticmei
 * @since 2019/07/18
 */
public interface IWeEventClient {
    /**
     * Get the client handler of WeEvent's broker with default groupId and url, http://localhost:8080/weevent.
     *
     * @return IWeEventClient WeEventClient struct
     * @throws BrokerException broker exception
     */
    static IWeEventClient build() throws BrokerException {
        return new WeEventClient();
    }

    /**
     * Get the client handler of WeEvent's broker with custom url.
     *
     * @param brokerUrl WeEvent's broker url, like http://localhost:8080/weevent
     * @return IWeEventClient WeEventClient struct
     * @throws BrokerException broker exception
     */
    static IWeEventClient build(String brokerUrl) throws BrokerException {
        return new WeEventClient(brokerUrl);
    }

    /**
     * Get the client handler of WeEvent's broker with custom groupId and url.
     *
     * @param brokerUrl WeEvent's broker url, like http://localhost:8080/weevent
     * @param groupId groupId
     * @return IWeEventClient WeEventClient struct
     * @throws BrokerException broker exception
     */
    static IWeEventClient build(String brokerUrl, String groupId) throws BrokerException {
        return new WeEventClient(brokerUrl, groupId);
    }

    /**
     * Get the client handler of WeEvent's broker custom url and account authorization.
     *
     * @param brokerUrl WeEvent's broker url, like http://localhost:8080/weevent
     * @param groupId groupId
     * @param userName account name
     * @param password password
     * @return IWeEventClient WeEventClient struct
     * @throws BrokerException broker exception
     */
    static IWeEventClient build(String brokerUrl, String groupId, String userName, String password) throws BrokerException {
        return new WeEventClient(brokerUrl, groupId, userName, password);
    }

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
     * Publish an event to topic.
     *
     * @param weEvent WeEvent(String topic, byte[] content, Map extensions)
     * @return send result, SendResult.SUCCESS if success, and SendResult.eventId
     * @throws BrokerException broker exception
     */
    SendResult publish(WeEvent weEvent) throws BrokerException;

    /**
     * Interface for notify callback
     */
    interface EventListener {
        /**
         * Called while new event arrived.
         *
         * @param event the event
         */
        void onEvent(WeEvent event);

        /**
         * Called while raise exception.
         *
         * @param e the e
         */
        void onException(Throwable e);
    }

    /**
     * Subscribe events from topic.
     *
     * @param topic topic name
     * @param offset from next event after this offset(an event id), WeEvent.OFFSET_FIRST if from head of queue, WeEvent.OFFSET_LAST if from tail of queue
     * @param listener callback
     * @return subscription Id
     * @throws BrokerException invalid input param
     */
    String subscribe(String topic, String offset, @NonNull EventListener listener) throws BrokerException;

    /**
     * Subscribe events from topic.
     *
     * @param topic topic name
     * @param offset from next event after this offset(an event id), WeEvent.OFFSET_FIRST if from head of queue, WeEvent.OFFSET_LAST if from tail of queue
     * @param subscriptionId keep last subscribe
     * @param listener callback
     * @return subscription Id
     * @throws BrokerException invalid input param
     */
    String subscribe(String topic, String offset, String subscriptionId, @NonNull EventListener listener) throws BrokerException;

    /**
     * Subscribe events from multiple topic.
     *
     * @param topics topic list
     * @param offset from next event after this offset(an event id), WeEvent.OFFSET_FIRST if from head of queue, WeEvent.OFFSET_LAST if from tail of queue
     * @param listener callback
     * @return subscription Id
     * @throws BrokerException invalid input param
     */
    String subscribe(String[] topics, String offset, @NonNull EventListener listener) throws BrokerException;

    /**
     * Subscribe events from multiple topic.
     *
     * @param topics topic list
     * @param offset from next event after this offset(an event id), WeEvent.OFFSET_FIRST if from head of queue, WeEvent.OFFSET_LAST if from tail of queue
     * @param subscriptionId keep last subscribe
     * @param listener callback
     * @return subscription Id
     * @throws BrokerException invalid input param
     */
    String subscribe(String[] topics, String offset, String subscriptionId, @NonNull EventListener listener) throws BrokerException;

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
}
