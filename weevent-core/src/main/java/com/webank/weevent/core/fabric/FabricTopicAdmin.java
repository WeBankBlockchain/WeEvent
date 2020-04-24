package com.webank.weevent.core.fabric;

import java.util.List;

import com.webank.weevent.client.BrokerException;
import com.webank.weevent.client.ErrorCode;
import com.webank.weevent.client.TopicInfo;
import com.webank.weevent.client.TopicPage;
import com.webank.weevent.client.WeEvent;
import com.webank.weevent.core.IEventTopic;
import com.webank.weevent.core.dto.ContractContext;
import com.webank.weevent.core.dto.GroupGeneral;
import com.webank.weevent.core.dto.ListPage;
import com.webank.weevent.core.dto.QueryEntity;
import com.webank.weevent.core.dto.TbBlock;
import com.webank.weevent.core.dto.TbNode;
import com.webank.weevent.core.dto.TbTransHash;
import com.webank.weevent.core.fabric.sdk.FabricDelegate;
import com.webank.weevent.core.fisco.util.ParamCheckUtils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * @author websterchen
 * @version v1.1
 * @since 2019/8/13
 */
@Slf4j
public class FabricTopicAdmin implements IEventTopic {
    protected FabricDelegate fabricDelegate;

    public FabricTopicAdmin(FabricDelegate fabricDelegate) {
        this.fabricDelegate = fabricDelegate;
    }

    @Override
    public boolean open(String topic, String channelName) throws BrokerException {
        log.info("open topic: {} channelName: {}", topic, channelName);

        ParamCheckUtils.validateTopicName(topic);
        validateChannelName(channelName);
        try {
            return fabricDelegate.getFabricMap().get(channelName).createTopic(topic);
        } catch (BrokerException e) {
            if (e.getCode() == ErrorCode.TOPIC_ALREADY_EXIST.getCode()) {
                return true;
            }
            throw e;
        }
    }

    @Override
    public boolean close(String topic, String channelName) throws BrokerException {
        log.info("close topic: {} channelName: {}", topic, channelName);

        ParamCheckUtils.validateTopicName(topic);
        validateChannelName(channelName);

        if (exist(topic, channelName)) {
            return true;
        }

        throw new BrokerException(ErrorCode.TOPIC_NOT_EXIST);
    }

    @Override
    public WeEvent getEvent(String eventId, String channelName) throws BrokerException {
        log.debug("getEvent function input param eventId: {}", eventId);

        validateChannelName(channelName);
        return fabricDelegate.getFabricMap().get(channelName).getEvent(eventId);
    }

    @Override
    public boolean exist(String topic, String channelName) throws BrokerException {
        ParamCheckUtils.validateTopicName(topic);
        validateChannelName(channelName);

        return fabricDelegate.getFabricMap().get(channelName).isTopicExist(topic);
    }

    @Override
    public TopicPage list(Integer pageIndex, Integer pageSize, String channelName) throws BrokerException {

        ParamCheckUtils.validatePagIndexAndSize(pageIndex, pageSize);
        validateChannelName(channelName);

        return fabricDelegate.getFabricMap().get(channelName).listTopicName(pageIndex, pageSize);
    }

    @Override
    public TopicInfo state(String topic, String channelName) throws BrokerException {
        ParamCheckUtils.validateTopicName(topic);
        validateChannelName(channelName);

        return fabricDelegate.getFabricMap().get(channelName).getTopicInfo(topic);
    }

    @Override
    public List<String> listGroupId() {

        return fabricDelegate.listChannel();
    }

    @Override
    public Long getBlockHeight(String channelName) throws BrokerException {
        validateChannelName(channelName);

        return fabricDelegate.getBlockHeight(channelName);
    }

    @Override
    public GroupGeneral getGroupGeneral(String channelName) throws BrokerException {
        validateChannelName(channelName);

        return fabricDelegate.getFabricMap().get(channelName).getGroupGeneral();

    }

    @Override
    public ListPage<TbTransHash> queryTransList(QueryEntity queryEntity) throws BrokerException {
        validateChannelName(queryEntity.getGroupId());
        ParamCheckUtils.validatePagIndexAndSize(queryEntity.getPageNumber(), queryEntity.getPageSize());
        return fabricDelegate.getFabricMap().get(queryEntity.getGroupId())
                .queryTransList(queryEntity.getBlockNumber(), queryEntity.getPkHash(), queryEntity.getPageNumber(), queryEntity.getPageSize());
    }

    @Override
    public ListPage<TbBlock> queryBlockList(QueryEntity queryEntity) throws BrokerException {
        validateChannelName(queryEntity.getGroupId());
        ParamCheckUtils.validatePagIndexAndSize(queryEntity.getPageNumber(), queryEntity.getPageSize());
        return fabricDelegate.getFabricMap().get(queryEntity.getGroupId())
                .queryBlockList(queryEntity.getBlockNumber(), queryEntity.getPkHash(), queryEntity.getPageNumber(), queryEntity.getPageSize());
    }

    @Override
    public ListPage<TbNode> queryNodeList(QueryEntity queryEntity) throws BrokerException {
        validateChannelName(queryEntity.getGroupId());

        return fabricDelegate.getFabricMap().get(queryEntity.getGroupId())
                .queryNodeList(queryEntity.getPageNumber(), queryEntity.getPageSize());
    }

    @Override
    public ContractContext getContractContext(String groupId) throws BrokerException {
        return null;
    }

    protected void validateChannelName(String channelName) throws BrokerException {
        log.debug("check channelName: {} exist. ", channelName);
        if (StringUtils.isBlank(channelName) || !fabricDelegate.listChannel().contains(channelName)) {
            throw new BrokerException(ErrorCode.FABRICSDK_CHANNEL_NAME_INVALID);
        }
    }

    @Override
    public boolean addOperator(String groupId, String topicName, String address) {
        return false;
    }

    @Override
    public boolean delOperator(String groupId, String topicName, String address) {
        return false;
    }

    @Override
    public List<String> listOperator(String groupId, String topicName) {
        return null;
    }
}
