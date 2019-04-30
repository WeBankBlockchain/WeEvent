package com.webank.weevent.broker.fisco;

import com.webank.weevent.broker.fisco.dto.ListPage;
import com.webank.weevent.broker.fisco.util.ParamCheckUtils;
import com.webank.weevent.broker.fisco.web3sdk.FiscoBcosDelegate;
import com.webank.weevent.broker.plugin.IEventTopic;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.ErrorCode;
import com.webank.weevent.sdk.TopicInfo;
import com.webank.weevent.sdk.TopicPage;
import com.webank.weevent.sdk.WeEvent;

import lombok.extern.slf4j.Slf4j;

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

    FiscoBcosTopicAdmin() throws BrokerException {
        this.fiscoBcosDelegate = new FiscoBcosDelegate();
        this.fiscoBcosDelegate.initProxy();
    }

    public FiscoBcosDelegate getFiscoBcosDelegate() {
        return this.fiscoBcosDelegate;
    }

    @Override
    public boolean open(String topic) throws BrokerException {
        ParamCheckUtils.validateTopicName(topic);

        try {
            boolean result = this.fiscoBcosDelegate.createTopic(topic, 1L);

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
    public boolean exist(String topic) throws BrokerException {
        ParamCheckUtils.validateTopicName(topic);

        boolean result = this.fiscoBcosDelegate.isTopicExist(topic, 1L);

        log.debug("isTopicExist result: {}", result);
        return result;
    }

    @Override
    public boolean close(String topic) throws BrokerException {
        ParamCheckUtils.validateTopicName(topic);

        if (exist(topic)) {
            return true;
        }

        throw new BrokerException(ErrorCode.TOPIC_NOT_EXIST);
    }

    /**
     * Get Blockchain All TopicInfo return 10 date Per page
     *
     * @since 2018/11/05
     */
    @Override
    public TopicPage list(Integer pageIndex, Integer pageSize) throws BrokerException {
        log.debug("list function input param, pageIndex: {} pageSize: {}", pageIndex, pageSize);

        if (pageIndex == null || pageIndex < 0) {
            throw new BrokerException(ErrorCode.TOPIC_PAGE_INDEX_INVALID);
        }
        if (pageSize == null || pageSize <= 0 || pageSize > 100) {
            throw new BrokerException(ErrorCode.TOPIC_PAGE_SIZE_INVALID);
        }

        @SuppressWarnings(value = "unchecked")
        ListPage<String> listPage = this.fiscoBcosDelegate.listTopicName(pageIndex, pageSize, 1L);

        TopicPage topicPage = new TopicPage();
        topicPage.setTotal(listPage.getTotal());
        topicPage.setPageIndex(listPage.getPageIndex());
        topicPage.setPageSize(listPage.getPageSize());
        for (String topic : listPage.getPageData()) {
            topicPage.getTopicInfoList().add(state(topic));
        }

        log.debug("block chain topic name list: {} block chain topic info list: {}", listPage, topicPage);
        return topicPage;
    }

    @Override
    public TopicInfo state(String topic) throws BrokerException {
        // fetch target topic info in block chain
        log.debug("state function input param topic: {}", topic);

        return this.fiscoBcosDelegate.getTopicInfo(topic, 1L);
    }

    @Override
    public WeEvent getEvent(String eventId) throws BrokerException {
        log.debug("getEvent function input param eventId: {}", eventId);

        return this.fiscoBcosDelegate.getEvent(eventId, 1L);
    }
}
