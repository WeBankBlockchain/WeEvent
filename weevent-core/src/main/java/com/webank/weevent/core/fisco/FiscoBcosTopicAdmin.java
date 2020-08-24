package com.webank.weevent.core.fisco;

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
import com.webank.weevent.core.fisco.util.ParamCheckUtils;
import com.webank.weevent.core.fisco.web3sdk.FiscoBcosDelegate;

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

    // FISCO-BCOS handler
    protected FiscoBcosDelegate fiscoBcosDelegate;

    public FiscoBcosTopicAdmin(FiscoBcosDelegate fiscoBcosDelegate) {
        this.fiscoBcosDelegate = fiscoBcosDelegate;
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

    @Override
    public Long getBlockHeight(String groupIdStr) throws BrokerException {
        String groupId = selectGroupId(groupIdStr);
        this.validateGroupId(groupId);
        return fiscoBcosDelegate.getBlockHeight(Long.parseLong(groupId));
    }

    @Override
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

    @Override
    public ContractContext getContractContext(String groupIdStr) throws BrokerException {
        String groupId = selectGroupId(groupIdStr);
        this.validateGroupId(groupId);
        return fiscoBcosDelegate.getContractContext(Long.valueOf(groupId));

    }

    @Override
    public boolean addOperator(String groupIdStr, String topicName, String operatorAddress) throws BrokerException {
        String groupId = selectGroupId(groupIdStr);
        this.validateGroupId(groupId);

        return fiscoBcosDelegate.addOperator(Long.parseLong(groupId), topicName, operatorAddress);
    }

    @Override
    public boolean delOperator(String groupIdStr, String topicName, String operatorAddress) throws BrokerException {
        String groupId = selectGroupId(groupIdStr);
        this.validateGroupId(groupId);

        return fiscoBcosDelegate.delOperator(Long.parseLong(groupId), topicName, operatorAddress);
    }

    @Override
    public List<String> listOperator(String groupIdStr, String topicName) throws BrokerException {
        String groupId = selectGroupId(groupIdStr);
        this.validateGroupId(groupId);

        return fiscoBcosDelegate.listOperator(Long.parseLong(groupId), topicName);
    }
}
