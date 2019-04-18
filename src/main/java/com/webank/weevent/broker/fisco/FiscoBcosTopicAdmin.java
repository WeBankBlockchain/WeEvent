package com.webank.weevent.broker.fisco;

import com.webank.weevent.broker.plugin.IEventTopic;
import com.webank.weevent.broker.fisco.dto.ListPage;
import com.webank.weevent.broker.fisco.dto.ResponseData;
import com.webank.weevent.broker.fisco.service.impl.TopicServiceImpl;
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

    protected TopicServiceImpl topicService;

    FiscoBcosTopicAdmin() {
        this.topicService = new TopicServiceImpl();
    }

    @Override
    public boolean open(String topic) throws BrokerException {
        try {
            ResponseData<Boolean> responseData = topicService.createTopic(topic);

            log.debug("createTopic result: {}", responseData);
            return responseData.getResult();
        } catch (BrokerException e) {
            if (e.getCode() == ErrorCode.TOPIC_ALREADY_EXIST.getCode()) {
                return true;
            }
            throw e;
        }
    }

    @Override
    public boolean exist(String topic) throws BrokerException {
        ResponseData<Boolean> responseData = topicService.isTopicExist(topic);

        log.debug("exist result: {}", responseData);
        return responseData.getResult();
    }

    @Override
    public boolean close(String topic) throws BrokerException {
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

        @SuppressWarnings(value = "unchecked")
        ListPage<String> listPage = topicService.listTopicName(pageIndex, pageSize).getResult();

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

        ResponseData<TopicInfo> responseData = topicService.getTopicInfo(topic);
        return responseData.getResult();
    }

    @Override
    public WeEvent getEvent(String eventId) throws BrokerException {
        log.debug("getEvent function input param eventId: {}", eventId);

        ResponseData<WeEvent> responseData = topicService.getEvent(eventId);
        return responseData.getResult();
    }
}