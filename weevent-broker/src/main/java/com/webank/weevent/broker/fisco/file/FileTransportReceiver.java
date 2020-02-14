package com.webank.weevent.broker.fisco.file;


import java.util.HashMap;

import com.webank.weevent.broker.fisco.FiscoBcosBroker4Consumer;
import com.webank.weevent.broker.plugin.IConsumer;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.WeEvent;

/**
 * File receiver.
 *
 * @author matthewliu
 * @since 2020/02/13
 */
public class FileTransportReceiver extends FileTransport implements IConsumer.ConsumerListener {
    private final FiscoBcosBroker4Consumer fiscoBcosBroker4Consumer;
    private final String groupId;
    private String subscriptionId;

    public FileTransportReceiver(FiscoBcosBroker4Consumer fiscoBcosBroker4Consumer, String groupId) {
        this.fiscoBcosBroker4Consumer = fiscoBcosBroker4Consumer;
        this.groupId = groupId;
    }

    public void start() throws BrokerException {
        // subscribe topic
        this.subscriptionId = this.fiscoBcosBroker4Consumer.subscribe(this.topic,
                this.groupId,
                WeEvent.OFFSET_LAST,
                new HashMap<>(),
                this);
    }

    public void stop() throws BrokerException {
        // unSubscribe topic
        this.fiscoBcosBroker4Consumer.unSubscribe(this.subscriptionId);

    }

    public void openChannel(String fileId) throws BrokerException {
        // subscribe channel topic for received file data
        String subEndian = getSubscribeEndianTopic(topic, fileId);

        this.subscribeAMOPTopic(subEndian);

        //send an event for transport begin
    }

    public void closeChannel(String fileId) throws BrokerException {
        // unsubscribe channel topic
        String subEndian = getSubscribeEndianTopic(topic, fileId);

        this.unSubscribeAMOPTopic(subEndian);
    }

    @Override
    public void onEvent(String subscriptionId, WeEvent event) {

    }

    @Override
    public void onException(Throwable e) {

    }

    public byte[] downloadChunk(String fileId, int chunkIndex) {
        return null;
    }
}
