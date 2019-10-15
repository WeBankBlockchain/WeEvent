package com.webank.weevent.broker.fabric;

import java.util.ArrayList;
import java.util.List;

import com.webank.weevent.BrokerApplication;
import com.webank.weevent.broker.fabric.config.FabricConfig;
import com.webank.weevent.broker.fabric.sdk.FabricDelegate;
import com.webank.weevent.broker.fisco.util.ParamCheckUtils;
import com.webank.weevent.broker.plugin.IEventTopic;
import com.webank.weevent.protocol.rest.entity.GroupGeneral;
import com.webank.weevent.protocol.rest.entity.QueryEntity;
import com.webank.weevent.protocol.rest.entity.TbBlock;
import com.webank.weevent.protocol.rest.entity.TbNode;
import com.webank.weevent.protocol.rest.entity.TbTransHash;
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
        return fabricDelegate.getFabricMap().get(channelName).createTopic(topic);
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
        checkChannelName(channelName);

        return fabricDelegate.getFabricMap().get(channelName).getEvent(eventId);
    }

    @Override
    public boolean exist(String topic, String channelName) throws BrokerException {
        ParamCheckUtils.validateTopicName(topic);
        checkChannelName(channelName);

        return fabricDelegate.getFabricMap().get(channelName).isTopicExist(topic);
    }

    @Override
    public TopicPage list(Integer pageIndex, Integer pageSize, String channelName) throws BrokerException {
        checkChannelName(channelName);

        return fabricDelegate.getFabricMap().get(channelName).listTopicName(pageIndex, pageSize);
    }

    @Override
    public TopicInfo state(String topic, String channelName) throws BrokerException {
        ParamCheckUtils.validateTopicName(topic);
        checkChannelName(channelName);

        return fabricDelegate.getFabricMap().get(channelName).getTopicInfo(topic);
    }

    @Override
    public List<String> listGroupId() {
        return channels;
    }

    @Override
    public GroupGeneral getGroupGeneral(String channelName) throws BrokerException {
        checkChannelName(channelName);

        return fabricDelegate.getFabricMap().get(channelName).getGroupGeneral();

    }

    @Override
    public List<TbTransHash> queryTransList(QueryEntity queryEntity) throws BrokerException {
        checkChannelName(queryEntity.getGroupId());

        return fabricDelegate.getFabricMap().get(queryEntity.getGroupId())
                .queryTransList(queryEntity.getPkHash(), queryEntity.getBlockNumber());
    }

    @Override
    public List<TbBlock> queryBlockList(QueryEntity queryEntity) throws BrokerException {
        checkChannelName(queryEntity.getGroupId());

        return fabricDelegate.getFabricMap().get(queryEntity.getGroupId())
                .queryBlockList(queryEntity.getPkHash(), queryEntity.getBlockNumber());
    }

    @Override
    public List<TbNode> queryNodeList(QueryEntity queryEntity) throws BrokerException {
        checkChannelName(queryEntity.getGroupId());

        return fabricDelegate.getFabricMap().get(queryEntity.getGroupId()).queryNodeList();
    }

    private void checkChannelName(String channelName) throws BrokerException {
        log.debug("check channelName: {} exist. ", channelName);
        if (StringUtils.isBlank(channelName) || !channels.contains(channelName)){
            throw new BrokerException(ErrorCode.EVENT_GROUP_ID_INVALID);
        }
    }

}
