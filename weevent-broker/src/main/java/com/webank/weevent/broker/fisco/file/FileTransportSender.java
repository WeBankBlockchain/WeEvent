package com.webank.weevent.broker.fisco.file;


import com.webank.weevent.sdk.BrokerException;

/**
 * File sender.
 *
 * @author matthewliu
 * @since 2020/02/13
 */
public class FileTransportSender extends FileTransport {
    public void start() throws BrokerException {

    }

    public void stop() throws BrokerException {

    }

    public void openChannel(String fileId) throws BrokerException {
        // open channel for received file transport status and then subscribe it
        String pubEndian = getPublishEndianTopic(topic, fileId);

        this.subscribeAMOPTopic(pubEndian);

        //send an event for transport begin
    }

    public void closeChannel(String fileId) throws BrokerException {
        // unsubscribe and then close channel
        String pubEndian = getPublishEndianTopic(topic, fileId);

        this.unSubscribeAMOPTopic(pubEndian);
    }

    public void send(String fileId, int chunkIndex, byte[] data) throws BrokerException {

    }
}
