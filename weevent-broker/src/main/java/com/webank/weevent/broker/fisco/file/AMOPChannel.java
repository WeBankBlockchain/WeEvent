package com.webank.weevent.broker.fisco.file;


import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * AMOP channel for file transport.
 *
 * @author matthewliu
 * @since 2020/02/16
 */
@Slf4j
@Data
public class AMOPChannel {
    private final static String publishEndian = "pub";
    private final static String subscribeEndian = "sub";

    private String amopSenderTopic;
    private String amopReceivedTopic;

    public static String genPublishEndianTopic(String topic, String fileId) {
        return topic + "/" + fileId + "/" + publishEndian;
    }

    public static String genSubscribeEndianTopic(String topic, String fileId) {
        return topic + "/" + fileId + "/" + subscribeEndian;
    }

    public void subscribeSender(String topic) {

    }

    public void subscribeReceiver(String topic) {

    }

    public void unSubscribe(String topic) {

    }

    public void sendEvent(String topic, FileEvent fileEvent) {

    }
}
