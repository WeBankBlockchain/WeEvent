package com.webank.weevent.governance.controller;


import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webank.weevent.governance.common.GovernanceException;
import com.webank.weevent.governance.common.GovernanceResult;
import com.webank.weevent.governance.entity.TopicEventCountEntity;
import com.webank.weevent.governance.service.TopicHistoricalService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
@RequestMapping(value = "/historicalData")
@Slf4j
public class TopicHistoricalController {

    @Autowired
    private TopicHistoricalService topicHistoricalService;

    @PostMapping("/list")
    public GovernanceResult historicalDataList(@RequestBody TopicEventCountEntity topicEventCountEntity, HttpServletRequest request,
                                               HttpServletResponse response) throws GovernanceException {
        log.info("get  historicalDataEntity:{} ", topicEventCountEntity);
        Map<String, List<Integer>> returnMap = topicHistoricalService.historicalDataList(topicEventCountEntity, request, response);
        return new GovernanceResult(returnMap);
    }

    @PostMapping("/eventList")
    public GovernanceResult eventList(@RequestBody TopicEventCountEntity topicEventCountEntity, HttpServletRequest request,
                                      HttpServletResponse response) throws GovernanceException {
        log.info("get  eventList:{} ", topicEventCountEntity);
        List<TopicEventCountEntity> eventList = topicHistoricalService.eventList(topicEventCountEntity, request);
        return new GovernanceResult(eventList);
    }

}
