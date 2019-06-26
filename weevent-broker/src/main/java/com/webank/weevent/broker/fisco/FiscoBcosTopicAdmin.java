package com.webank.weevent.broker.fisco;

import com.webank.weevent.BrokerApplication;
import com.webank.weevent.broker.fisco.dto.ListPage;
import com.webank.weevent.broker.fisco.util.ParamCheckUtils;
import com.webank.weevent.broker.fisco.web3sdk.FiscoBcosDelegate;
import com.webank.weevent.broker.plugin.IEventTopic;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.ErrorCode;
import com.webank.weevent.sdk.TopicInfo;
import com.webank.weevent.sdk.TopicPage;
import com.webank.weevent.sdk.WeEvent;

import javafx.application.Application;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.core.ApplicationContext;
import org.springframework.boot.SpringApplication;

/**
 * Topic level's admin api. The underlying implement is a routing contract in
 * block chain.
 *
 * @since 2018/11/05
 */
@Slf4j
public class FiscoBcosTopicAdmin implements IEventTopic {
    // FISCO-BCOS handler
    protected static FiscoBcosDelegate fiscoBcosDelegate;

    static {
        try {
            fiscoBcosDelegate = new FiscoBcosDelegate();
            fiscoBcosDelegate.initProxy();
        } catch (BrokerException e) {
            log.error("init fisco-bcos failed", e);
            System.exit(SpringApplication.exit(BrokerApplication.applicationContext));
        }
    }

    @Override
    public boolean open(String topic, String groupId) throws BrokerException {
        ParamCheckUtils.validateTopicName(topic);
        ParamCheckUtils.validateGroupId(groupId);
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
    public boolean exist(String topic, String groupId) throws BrokerException {
        ParamCheckUtils.validateTopicName(topic);
        ParamCheckUtils.validateGroupId(groupId);
        boolean result = fiscoBcosDelegate.isTopicExist(topic, Long.parseLong(groupId));

        log.debug("isTopicExist result: {}", result);
        return result;
    }

    @Override
    public boolean close(String topic, String groupId) throws BrokerException {
        ParamCheckUtils.validateTopicName(topic);
        ParamCheckUtils.validateGroupId(groupId);
        if (exist(topic, groupId)) {
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
    public TopicPage list(Integer pageIndex, Integer pageSize, String groupId) throws BrokerException {
        log.debug("list function input param, pageIndex: {} pageSize: {}", pageIndex, pageSize);

        if (pageIndex == null || pageIndex < 0) {
            throw new BrokerException(ErrorCode.TOPIC_PAGE_INDEX_INVALID);
        }
        if (pageSize == null || pageSize <= 0 || pageSize > 100) {
            throw new BrokerException(ErrorCode.TOPIC_PAGE_SIZE_INVALID);
        }
        ParamCheckUtils.validateGroupId(groupId);
        @SuppressWarnings(value = "unchecked")
        ListPage<String> listPage = fiscoBcosDelegate.listTopicName(pageIndex, pageSize, Long.parseLong(groupId));

        TopicPage topicPage = new TopicPage();
        topicPage.setTotal(listPage.getTotal());
        topicPage.setPageIndex(listPage.getPageIndex());
        topicPage.setPageSize(listPage.getPageSize());
        for (String topic : listPage.getPageData()) {
            topicPage.getTopicInfoList().add(state(topic, groupId));
        }

        log.debug("block chain topic name list: {} block chain topic info list: {}", listPage, topicPage);
        return topicPage;
    }

    @Override
    public TopicInfo state(String topic, String groupId) throws BrokerException {
        // fetch target topic info in block chain
        log.debug("state function input param topic: {}", topic);
        ParamCheckUtils.validateGroupId(groupId);
        ParamCheckUtils.validateTopicName(topic);
        return fiscoBcosDelegate.getTopicInfo(topic, Long.parseLong(groupId));
    }

    @Override
    public WeEvent getEvent(String eventId, String groupId) throws BrokerException {
        log.debug("getEvent function input param eventId: {}", eventId);
        ParamCheckUtils.validateGroupId(groupId);
        return fiscoBcosDelegate.getEvent(eventId, Long.parseLong(groupId));
    }
}
