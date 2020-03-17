package com.webank.weevent.client;

import java.io.IOException;

import lombok.NonNull;

/**
 * Java Client for WeEvent
 *
 * @author matthewliu
 * @since 2019/07/18
 */
public interface IWeEventClient {
    String defaultBrokerUrl = "http://localhost:8080/weevent-broker";

    /**
     * builder class
     */
    class Builder {
        // broker url
        private String brokerUrl = defaultBrokerUrl;
        // group id
        private String groupId = WeEvent.DEFAULT_GROUP_ID;
        // stomp's account&password
        private String userName = "";
        private String password = "";
        // rpc timeout, ms
        private int timeout = 5000;

        public Builder brokerUrl(String brokerUrl) {
            this.brokerUrl = brokerUrl;
            return this;
        }

        public Builder groupId(String groupId) {
            this.groupId = groupId;
            return this;
        }

        public Builder userName(String userName) {
            this.userName = userName;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder timeout(int timeout) {
            this.timeout = timeout;
            return this;
        }

        public IWeEventClient build() throws BrokerException {
            return new WeEventClient(this.brokerUrl, this.groupId, this.userName, this.password, this.timeout);
        }
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
     * Interface for event notify callback
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
     * @param listener notify interface
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
     * @param listener notify interface
     * @return subscription Id
     * @throws BrokerException invalid input param
     */
    String subscribe(String topic, String offset, String subscriptionId, @NonNull EventListener listener) throws BrokerException;

    /**
     * Subscribe events from multiple topic.
     *
     * @param topics topic list
     * @param offset from next event after this offset(an event id), WeEvent.OFFSET_FIRST if from head of queue, WeEvent.OFFSET_LAST if from tail of queue
     * @param listener notify interface
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
     * @param listener notify interface
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

    // The following's is for big file's Pub/Sub

    /**
     * Publish a file to topic.
     * The file's data DO NOT stored in block chain. Yes, it's not persist, may be deleted sometime after subscribe notify.
     *
     * @param topic binding topic
     * @param localFile local file to be send
     * @return send result, SendResult.SUCCESS if success, and return SendResult.eventId
     * @throws BrokerException broker exception
     * @throws IOException IOException
     * @throws InterruptedException InterruptedException
     */
    SendResult publishFile(String topic, String localFile) throws BrokerException, IOException, InterruptedException;

    /**
     * Interface for file notify callback
     */
    interface FileListener {
        /**
         * Called while new file arrived.
         *
         * @param subscriptionId binding subscription
         * @param localFile local file with data
         */
        void onFile(String subscriptionId, String localFile);

        /**
         * Called while raise exception.
         *
         * @param e the e
         */
        void onException(Throwable e);
    }

    /**
     * Subscribe file from topic.
     *
     * @param topic topic name
     * @param filePath file path to store arriving file
     * @param fileListener notify interface
     * @return subscription Id
     * @throws BrokerException broker exception
     */
    String subscribeFile(String topic, String filePath, @NonNull FileListener fileListener) throws BrokerException;
}
