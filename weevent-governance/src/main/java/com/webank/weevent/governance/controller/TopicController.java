package com.webank.weevent.governance.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webank.weevent.governance.entity.TopicCreateEntity;
import com.webank.weevent.governance.entity.TopicEntity;
import com.webank.weevent.governance.entity.TopicPage;
import com.webank.weevent.governance.entity.TopicPageEntity;
import com.webank.weevent.governance.exception.GovernanceException;
import com.webank.weevent.governance.result.GovernanceResult;
import com.webank.weevent.governance.service.TopicService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
@RequestMapping(value = "topic")
@Slf4j
public class TopicController {

    @Autowired
    private TopicService topicService;

    @RequestMapping(value = "/close")
    public Boolean close(@RequestParam("brokerId") Integer brokerId, @RequestParam String topic, @RequestParam(required = false) String groupId,
                         HttpServletRequest request, HttpServletResponse response) throws GovernanceException {
        log.info("close topic ,brokerId:{},topic:{},groupId:{}", brokerId, topic, groupId);
        return topicService.close(brokerId, topic, groupId, request, response);

    }

    @RequestMapping(value = "/list")
    public TopicPage getTopics(@Validated @RequestBody TopicPageEntity topicPageEntity, HttpServletRequest request,
                               HttpServletResponse response) throws GovernanceException {

        log.info("get topic list,topicPageEntity:{}", topicPageEntity);
        return topicService.getTopics(topicPageEntity, request, response);
    }

    @RequestMapping(value = "/openTopic")
    public GovernanceResult open(@RequestBody TopicCreateEntity topicCreateEntity, HttpServletRequest request,
                                 HttpServletResponse response) throws GovernanceException {
        log.info("open topic creator:{} ,topic:{}", topicCreateEntity.getCreater(), topicCreateEntity.getTopic());
        return topicService.open(topicCreateEntity.getBrokerId(), topicCreateEntity.getTopic(),
                topicCreateEntity.getCreater(), topicCreateEntity.getGroupId(), request, response);
    }

    @RequestMapping(value = "/topicInfo")
    public TopicEntity getTopicInfo(@RequestParam(name = "brokerId") Integer brokerId,
                                    @RequestParam(name = "topic") String topic,
                                    @RequestParam(name = "groupId", required = false) String groupId,
                                    HttpServletRequest request) throws GovernanceException {

        log.info("get topicInfo,brokerId: {}, topicName: {}, groupId: {}", brokerId, topic, groupId);
        return topicService.getTopicInfo(brokerId, topic, groupId, request);
    }

    @PostMapping(value = "/destinationList")
    public GovernanceResult destinationList(@Validated @RequestBody TopicPageEntity topicPageEntity, HttpServletRequest request,
                                            HttpServletResponse response) throws GovernanceException {
        log.info("get destinationList,topicPageEntity:{}", topicPageEntity);
        topicPageEntity.setPageIndex(1);
        topicPageEntity.setPageSize(Integer.MAX_VALUE);
        TopicPage topics = topicService.getTopics(topicPageEntity, request, response);
        return new GovernanceResult(topics);
    }
}
