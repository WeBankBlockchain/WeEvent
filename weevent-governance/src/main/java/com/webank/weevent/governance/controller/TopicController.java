package com.webank.weevent.governance.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webank.weevent.governance.entity.Topic;
import com.webank.weevent.governance.entity.TopicCreaterDto;
import com.webank.weevent.governance.entity.TopicPage;
import com.webank.weevent.governance.entity.TopicPageDto;
import com.webank.weevent.governance.exception.GovernanceException;
import com.webank.weevent.governance.result.GovernanceResult;
import com.webank.weevent.governance.service.TopicService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
@RequestMapping(value = "topic")
@Slf4j
public class TopicController {

    @Autowired
    TopicService topicService;

    /**
     * just for test...
     * 
     * @return
     */
    @RequestMapping(value = "/hello", method = RequestMethod.GET)
    public String hello() {
        log.info("Hello World test...");
        return "Hello World!";
    }

    @RequestMapping(value = "/close")
    public Boolean close(@RequestParam("brokerId") Integer brokerId, @RequestParam String topic,
            HttpServletRequest request, HttpServletResponse response) throws GovernanceException {
        log.info("brokerId:" + brokerId + "close: " + topic);
        return topicService.close(brokerId, topic, request, response);

    }

    @RequestMapping(value = "/list")
    public TopicPage getTopics(@RequestBody TopicPageDto topicPageDto, HttpServletRequest request,
            HttpServletResponse response) throws GovernanceException {

        log.info("pageIndex: " + topicPageDto.getPageIndex() + " pageSize: " + topicPageDto.getPageSize());
        return topicService.getTopics(topicPageDto.getBrokerId(), topicPageDto.getPageIndex(),
                topicPageDto.getPageSize(), request, response);
    }

    @RequestMapping(value = "/openTopic")
    public GovernanceResult open(@RequestBody TopicCreaterDto topicCreaterDto, HttpServletRequest request,
            HttpServletResponse response) throws GovernanceException {
        log.info("creater: " + topicCreaterDto.getCreater() + " open: " + topicCreaterDto.getTopic());
        return topicService.open(topicCreaterDto.getBrokerId(), topicCreaterDto.getTopic(),
                topicCreaterDto.getCreater(), request, response);
    }

    @RequestMapping(value = "/topicInfo")
    public Topic getTopicInfo(@RequestParam(name = "brokerId") Integer brokerId,
                              @RequestParam(name = "topic") String topic,
                              @RequestParam(name = "groupId", required = false) String groupId,
                              HttpServletRequest request) throws GovernanceException {

        log.info("brokerId: {}, topicName: {}, groupId: {}", brokerId, topic, groupId);
        return topicService.getTopicInfo(brokerId, topic, groupId, request);
    }
}
