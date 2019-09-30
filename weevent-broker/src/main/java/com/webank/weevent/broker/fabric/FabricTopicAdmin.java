package com.webank.weevent.broker.fabric;

import java.util.ArrayList;
import java.util.List;

import com.webank.weevent.BrokerApplication;
import com.webank.weevent.broker.fabric.config.FabricConfig;
import com.webank.weevent.broker.fabric.sdk.FabricDelegate;
import com.webank.weevent.broker.plugin.IEventTopic;
import com.webank.weevent.broker.util.ParamCheckUtils;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.ErrorCode;
import com.webank.weevent.sdk.TopicInfo;
import com.webank.weevent.sdk.TopicPage;
import com.webank.weevent.sdk.WeEvent;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * @author websterchen
 * @version v1.1
 * @since 2019/8/13
 */
@Slf4j
public class FabricTopicAdmin implements IEventTopic {
    protected static FabricDelegate fabricDelegate;
    protected static FabricConfig fabricConfig;
    protected static List<String> channels = new ArrayList<>();

    static {
        fabricConfig = new FabricConfig();
        if (!fabricConfig.load()) {
            log.error("load Fabric configuration failed");
            BrokerApplication.exit();
        }
        try {
            fabricDelegate = new FabricDelegate();
            fabricDelegate.initProxy(fabricConfig);
            channels = fabricDelegate.listChannel();
        } catch (BrokerException e) {
            log.error("init Fabric failed", e);
            BrokerApplication.exit();
        }
    }

    @Override
    public boolean open(String topic, String channelName) throws BrokerException {
        ParamCheckUtils.validateTopicName(topic);
        checkChannelName(channelName);
        return fabricDelegate.getFabricMap().get(fabricConfig.getChannelName()).createTopic(topic);
    }

    @Override
    public boolean close(String topic, String channelName) throws BrokerException {
        log.info("close topic: {} channelName: {}", topic, channelName);

        ParamCheckUtils.validateTopicName(topic);
        checkChannelName(channelName);

        if (exist(topic, channelName)) {
            return true;
        }

        throw new BrokerException(ErrorCode.TOPIC_NOT_EXIST);
    }

    @Override
    public WeEvent getEvent(String eventId, String channelName) throws BrokerException {
        return null;
    }

    @Override
    public boolean exist(String topic, String channelName) throws BrokerException {
        ParamCheckUtils.validateTopicName(topic);
        checkChannelName(channelName);

        return fabricDelegate.getFabricMap().get(fabricConfig.getChannelName()).isTopicExist(topic);
    }

    @Override
    public TopicPage list(Integer pageIndex, Integer pageSize, String channelName) throws BrokerException {
        checkChannelName(channelName);
        return null;
    }

    @Override
    public TopicInfo state(String topic, String channelName) throws BrokerException {
        ParamCheckUtils.validateTopicName(topic);
        checkChannelName(channelName);

        return fabricDelegate.getFabricMap().get(fabricConfig.getChannelName()).getTopicInfo(topic);
    }

    @Override
    public List<String> listGroupId() {
        return channels;
    }

    private void checkChannelName(String channelName) throws BrokerException {
        log.debug("check channelName: {} exist. ", channelName);
        if (StringUtils.isBlank(channelName) || !channels.contains(channelName)){
            throw new BrokerException(ErrorCode.EVENT_GROUP_ID_INVALID);
        }
    }

}
