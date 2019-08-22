package com.webank.weevent.broker.fabric;

import java.util.Map;

import com.webank.weevent.broker.fisco.FiscoBcosTopicAdmin;
import com.webank.weevent.broker.plugin.IConsumer;
import com.webank.weevent.sdk.BrokerException;

public class FabricBroker4Consumer extends FiscoBcosTopicAdmin implements IConsumer {
    @Override
    public String subscribe(String[] topics, String groupId, String offset, String interfaceType, ConsumerListener listener) throws BrokerException {
        return null;
    }

    @Override
    public String subscribe(String topic, String groupId, String offset, String interfaceType, ConsumerListener listener) throws BrokerException {
        return null;
    }

    @Override
    public String subscribe(String topic, String groupId, String offset, String subscriptionId, String interfaceType, ConsumerListener listener) throws BrokerException {
        return null;
    }

    @Override
    public boolean unSubscribe(String subscriptionId) throws BrokerException {
        return false;
    }

    @Override
    public boolean isStarted() {
        return false;
    }

    public boolean startConsumer() throws BrokerException {
        return false;
    }

    public boolean shutdownConsumer() {
        return false;
    }

    public Map<String, Object> listSubscription() {
        return null;
    }
}
