package com.webank.weevent.broker.fabric;

import java.util.List;

import com.webank.weevent.BrokerApplication;
import com.webank.weevent.broker.fabric.config.FabricConfig;
import com.webank.weevent.broker.fabric.sdk.FabricDelegate;
import com.webank.weevent.broker.plugin.IEventTopic;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.TopicInfo;
import com.webank.weevent.sdk.TopicPage;
import com.webank.weevent.sdk.WeEvent;

import lombok.extern.slf4j.Slf4j;

/**
 * @author websterchen
 * @version v1.1
 * @since 2019/8/13
 */
@Slf4j
public class FabricTopicAdmin implements IEventTopic {
    protected static FabricDelegate fabricDelegate;
    protected static FabricConfig fabricConfig;

    static {
        fabricConfig = new FabricConfig();
        if (!fabricConfig.load()) {
            log.error("load Fabric configuration failed");
            BrokerApplication.exit();
        }
        try {
            fabricDelegate = new FabricDelegate();
            fabricDelegate.initProxy(fabricConfig);
        } catch (BrokerException e) {
            log.error("init Fabric failed", e);
            BrokerApplication.exit();
        }
    }

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
    public boolean exist(String topic, String channelName) throws BrokerException {
        return true;
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
    public List<String> listGroupId() throws BrokerException {
        return null;
    }
}
