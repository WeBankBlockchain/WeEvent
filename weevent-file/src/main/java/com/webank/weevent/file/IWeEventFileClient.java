package com.webank.weevent.file;

import com.webank.weevent.client.BrokerException;
import com.webank.weevent.client.SendResult;
import com.webank.weevent.core.config.FiscoConfig;
import com.webank.weevent.file.dto.FileChunksMetaPlus;
import com.webank.weevent.file.dto.FileTransportStats;
import com.webank.weevent.file.service.FileChunksMeta;
import com.webank.weevent.file.service.WeEventFileClient;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface IWeEventFileClient {
    public static IWeEventFileClient build(String groupId, String filePath, int fileChunkSize, FiscoConfig fiscoConfig) {
        return new WeEventFileClient(groupId, filePath, fileChunkSize, fiscoConfig);
    }

    /**
     * @param topic topic name
     */
    void openTransport4Sender(String topic);

    /**
     * @param topic topic name
     * @param publicPem public pem inputstream
     * @throws BrokerException broker exception
     */
    void openTransport4Sender(String topic, InputStream publicPem) throws BrokerException;

    /**
     * @param topic topic name
     * @param publicPem public pem path string
     * @throws BrokerException broker exception
     * @throws IOException IOException
     */
    void openTransport4Sender(String topic, String publicPem) throws BrokerException, IOException;

    /**
     * Publish a file to topic.
     * The file's data DO NOT stored in block chain. Yes, it's not persist, may be deleted sometime after subscribe notify.
     *
     * @param topic binding topic
     * @param localFile local file to be send
     * @param overwrite if receiver has this file, overwrite it?
     * @return send result, SendResult.SUCCESS if success, and return SendResult.eventId
     * @throws BrokerException broker exception
     * @throws IOException IOException
     * @throws InterruptedException InterruptedException
     */
    FileChunksMeta publishFile(String topic, String localFile, boolean overwrite) throws BrokerException, IOException, InterruptedException;

    /**
     * @param topic topic name
     * @param fileListener notify interface
     * @throws BrokerException broker exception
     */
    void openTransport4Receiver(String topic, FileListener fileListener) throws BrokerException;

    /**
     * @param topic topic name
     * @param fileListener notify interface
     * @param privatePem private key pem inputstream
     * @throws BrokerException broker exception
     */
    void openTransport4Receiver(String topic, FileListener fileListener, InputStream privatePem) throws BrokerException;

    /**
     * @param topic topic name
     * @param fileListener notify interface
     * @param privatePem private key pem path string
     * @throws IOException IOException
     * @throws BrokerException InterruptedException
     */
    void openTransport4Receiver(String topic, FileListener fileListener, String privatePem) throws IOException, BrokerException;

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
     * invoke unSubTopic
     *
     * @param topic topic name
     */
    void closeTransport(String topic);

    /**
     * @param topic topic name
     * @return filetransportstatus
     */
    FileTransportStats status(String topic);

    /**
     * @param topic topic name
     * @return filechunksmeta list
     * @throws BrokerException broker exception
     */
    List<FileChunksMeta> listFiles(String topic) throws BrokerException;

    /**
     * @param fileChunksMeta fileChunksMeta
     * @return send result and eventId
     * @throws BrokerException broker exception
     */
    SendResult sign(FileChunksMeta fileChunksMeta) throws BrokerException;

    /**
     * @param eventId eventId return by sign
     * @param groupId group id
     * @return file and block information
     * @throws BrokerException broker exception
     */
    FileChunksMetaPlus verify(String eventId, String groupId) throws BrokerException;
}
