package com.webank.weevent.broker.fisco;

import java.util.List;

import com.webank.weevent.BrokerApplication;
import com.webank.weevent.broker.config.FiscoConfig;
import com.webank.weevent.broker.fisco.dto.ListPage;
import com.webank.weevent.broker.fisco.util.ParamCheckUtils;
import com.webank.weevent.broker.fisco.web3sdk.FiscoBcosDelegate;
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
 * Topic level's admin api. The underlying implement is a routing contract in
 * block chain.
 *
 * @since 2018/11/05
 */
@Slf4j
public class FiscoBcosTopicAdmin implements IEventTopic {
    // FISCO-BCOS config
    protected static FiscoConfig fiscoConfig;

    // FISCO-BCOS handler
    protected static FiscoBcosDelegate fiscoBcosDelegate;

    static {
        FiscoConfig config = new FiscoConfig();
        if (!config.load()) {
            log.error("load FISCO-BCOS configuration failed");
            BrokerApplication.exit();
        }
        fiscoConfig = config;

        try {
            fiscoBcosDelegate = new FiscoBcosDelegate();
            fiscoBcosDelegate.initProxy(fiscoConfig);
        } catch (BrokerException e) {
            log.error("init FISCO-BCOS failed", e);
            BrokerApplication.exit();
        }
    }

    @Override
    public boolean open(String topic, String groupIdStr) throws BrokerException {
        log.info("open topic: {} groupId: {}", topic, groupIdStr);

        String groupId = selectGroupId(groupIdStr);
        this.validateGroupId(groupId);
        ParamCheckUtils.validateTopicName(topic);
        try {
            boolean result = fiscoBcosDelegate.createTopic(topic, Long.parseLong(groupId));

            log.debug("createTopic result: {}", result);

            return result;
        } catch (BrokerException e) {
            if (e.getCode() == ErrorCode.TOPIC_ALREADY_EXIST.getCode()) {
                return true;
            }
            throw e;
        }
    }

    @Override
    public boolean exist(String topic, String groupIdStr) throws BrokerException {
        String groupId = selectGroupId(groupIdStr);
        this.validateGroupId(groupId);
        ParamCheckUtils.validateTopicName(topic);
        boolean result = fiscoBcosDelegate.isTopicExist(topic, Long.parseLong(groupId));

        log.debug("isTopicExist result: {}", result);
        return result;
    }

    @Override
    public boolean close(String topic, String groupIdStr) throws BrokerException {
        log.info("close topic: {} groupId: {}", topic, groupIdStr);

        String groupId = selectGroupId(groupIdStr);
        this.validateGroupId(groupId);
        ParamCheckUtils.validateTopicName(topic);
        if (exist(topic, groupId)) {
            return true;
        }

        throw new BrokerException(ErrorCode.TOPIC_NOT_EXIST);
    }

    /**
     * Get All TopicInfo return 10 date Per page
     *
     * @since 2018/11/05
     */
    @Override
    public TopicPage list(Integer pageIndex, Integer pageSize, String groupIdStr) throws BrokerException {
        log.debug("list function input param, pageIndex: {} pageSize: {}", pageIndex, pageSize);

        ParamCheckUtils.validatePagIndexAndSize(pageIndex, pageSize);
        String groupId = selectGroupId(groupIdStr);
        this.validateGroupId(groupId);

        Long groupIdLong = Long.parseLong(groupId);
        ListPage<String> listPage = fiscoBcosDelegate.listTopicName(pageIndex, pageSize, groupIdLong);

        TopicPage topicPage = new TopicPage();
        topicPage.setTotal(listPage.getTotal());
        topicPage.setPageIndex(listPage.getPageIndex());
        topicPage.setPageSize(listPage.getPageSize());
        for (String topic : listPage.getPageData()) {
            // use cache is ok
            topicPage.getTopicInfoList().add(fiscoBcosDelegate.getTopicInfo(topic, groupIdLong, false));
        }

        log.debug("block chain topic name list: {} block chain topic info list: {}", listPage, topicPage);
        return topicPage;
    }

    @Override
    public TopicInfo state(String topic, String groupIdStr) throws BrokerException {
        // fetch target topic info in block chain
        log.debug("state function input param topic: {}", topic);

        String groupId = selectGroupId(groupIdStr);
        this.validateGroupId(groupId);
        ParamCheckUtils.validateTopicName(topic);

        // state force to query block, can not use local cache
        return fiscoBcosDelegate.getTopicInfo(topic, Long.parseLong(groupId), true);
    }

    @Override
    public WeEvent getEvent(String eventId, String groupIdStr) throws BrokerException {
        log.debug("getEvent function input param eventId: {}", eventId);
        String groupId = selectGroupId(groupIdStr);
        this.validateGroupId(groupId);
        return fiscoBcosDelegate.getEvent(eventId, Long.parseLong(groupId));
    }

    @Override
    public List<String> listGroupId() throws BrokerException {
        return fiscoBcosDelegate.listGroupId();
    }

    public void validateGroupId(String groupId) throws BrokerException {
        ParamCheckUtils.validateGroupId(groupId, fiscoBcosDelegate.listGroupId());
    }

    @Override
    public GroupGeneral getGroupGeneral(String groupIdStr) throws BrokerException {
        String groupId = selectGroupId(groupIdStr);
        this.validateGroupId(groupId);
        return fiscoBcosDelegate.getGroupGeneral(Long.valueOf(groupId));
    }

    @Override
    public ListPage<TbTransHash> queryTransList(QueryEntity queryEntity) throws BrokerException {
        String groupId = selectGroupId(queryEntity.getGroupId());
        this.validateGroupId(groupId);
        ParamCheckUtils.validatePagIndexAndSize(queryEntity.getPageNumber(), queryEntity.getPageSize());
        return fiscoBcosDelegate.queryTransList(Long.valueOf(groupId), queryEntity.getPkHash(), queryEntity.getBlockNumber(),
                queryEntity.getPageNumber(), queryEntity.getPageSize());
    }

    @Override
    public ListPage<TbBlock> queryBlockList(QueryEntity queryEntity) throws BrokerException {
        String groupId = selectGroupId(queryEntity.getGroupId());
        this.validateGroupId(groupId);
        ParamCheckUtils.validatePagIndexAndSize(queryEntity.getPageNumber(), queryEntity.getPageSize());
        return fiscoBcosDelegate.queryBlockList(Long.valueOf(groupId), queryEntity.getPkHash(), queryEntity.getBlockNumber(),
                queryEntity.getPageNumber(), queryEntity.getPageSize());
    }

    @Override
    public ListPage<TbNode> queryNodeList(QueryEntity queryEntity) throws BrokerException {
        String groupId = selectGroupId(queryEntity.getGroupId());
        this.validateGroupId(groupId);
        return fiscoBcosDelegate.queryNodeList(Long.valueOf(groupId));
    }

    public String selectGroupId(String groupId) {
        return StringUtils.isBlank(groupId) ? WeEvent.DEFAULT_GROUP_ID : groupId;
    }

}
