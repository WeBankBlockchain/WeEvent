package com.webank.weevent.broker.fabric.sdk;

import com.webank.weevent.broker.fabric.config.FabricConfig;
import com.webank.weevent.sdk.BrokerException;

public class Fabric {
    //config
    private FabricConfig fabricConfig;

    public void getTopic() throws BrokerException {

    }

    public boolean createTopic() throws BrokerException {
        return false;
    }

    public boolean isTopicExist(String topicName) throws BrokerException {
        return false;
    }

    public void listTopicName(Integer pageIndex, Integer pageSize) throws BrokerException {

    }

    public void getTopicInfo(String topicName) throws BrokerException {

    }

    public void getEvent(String eventId) throws BrokerException {

    }

    public void publishEvent(String topicName, String eventContent, String extensions) throws BrokerException {

    }

    public Long getBlockHeight() throws BrokerException {
        return 0L;
    }
}
