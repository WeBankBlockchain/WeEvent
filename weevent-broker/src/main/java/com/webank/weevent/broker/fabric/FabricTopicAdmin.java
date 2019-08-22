package com.webank.weevent.broker.fabric;

import java.util.List;

import com.webank.weevent.broker.plugin.IEventTopic;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.TopicInfo;
import com.webank.weevent.sdk.TopicPage;
import com.webank.weevent.sdk.WeEvent;

public class FabricTopicAdmin implements IEventTopic {
    @Override
    public boolean open(String topic, String groupId) throws BrokerException {
        return false;
    }

    @Override
    public boolean close(String topic, String groupId) throws BrokerException {
        return false;
    }

    @Override
    public WeEvent getEvent(String eventId, String groupId) throws BrokerException {
        return null;
    }

    @Override
    public boolean exist(String topic, String groupId) throws BrokerException {
        return false;
    }

    @Override
    public TopicPage list(Integer pageIndex, Integer pageSize, String groupId) throws BrokerException {
        return null;
    }

    @Override
    public TopicInfo state(String topic, String groupId) throws BrokerException {
        return null;
    }

    @Override
    public List<String> listGroupId() throws BrokerException{
        return null;
    }
}
