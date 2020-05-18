package com.webank.weevent.file;

import com.webank.weevent.client.*;
import com.webank.weevent.core.config.FiscoConfig;
import com.webank.weevent.file.dto.FileTransportStats;
import com.webank.weevent.file.service.FileChunksMeta;
import lombok.NonNull;

import java.io.IOException;
import java.io.InputStream;

public interface IWeEventFileClient {
    public static IWeEventFileClient build(FiscoConfig fiscoConfig, String groupId) {
        return null;
    }

    /**
     * publish file
     *
     * @param topic topic name
     * @throws BrokerException exception
     */
    void openTransport4Sender(String topic) throws BrokerException;
    void openTransport4Sender(String topic, InputStream publicPem) throws BrokerException;
    void openTransport4Sender(String topic, String publicPem);


    /**l
     * subscribe file
     *
     * @param topic topic name
     * @param filePath receive file in filePath
     * @param fileListener file listener call back
     */
    void openTransport4Receiver(String topic, String filePath, FileListener fileListener);
    void openTransport4Receiver(String topic, String filePath, FileListener fileListener, InputStream privatePem);
    void openTransport4Receiver(String topic, String filePath, FileListener fileListener, String privatePem);

    /**
     * invoke unSubTopic
     *
     * @param topic topic name
     */
    void closeTransport(String topic);
    FileTransportStats status(String topic);
    FileChunksMeta listFiles(String topic);

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
         * @param topicName topic name
         * @param fileName  file name
         */
        void onFile(String topicName, String fileName);

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
     * @throws BrokerException broker exception
     */
    void  subscribeFile(String topic, String filePath, @NonNull FileListener fileListener) throws BrokerException;
}
