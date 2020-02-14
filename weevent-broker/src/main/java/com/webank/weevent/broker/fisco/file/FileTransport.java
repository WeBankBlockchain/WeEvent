package com.webank.weevent.broker.fisco.file;


/**
 * Transport file through the FISCO-BCOS's p2p network.
 *
 * @author matthewliu
 * @since 2020/02/13
 */
public class FileTransport {
    private final static String publishEndian = "pub";
    private final static String subscribeEndian = "sub";
    protected final static String topic = "com/weevent/file";

    public String getPublishEndianTopic(String topic, String fileId) {
        return topic + "/" + fileId + "/" + publishEndian;
    }

    public String getSubscribeEndianTopic(String topic, String fileId) {
        return topic + "/" + fileId + "/" + subscribeEndian;
    }

    public void subscribeAMOPTopic(String topic) {

    }

    public void unSubscribeAMOPTopic(String topic) {

    }
}
