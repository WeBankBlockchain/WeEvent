package com.webank.weevent.governance.controller;


import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webank.weevent.governance.entity.TopicTopicHistoricalEntity;
import com.webank.weevent.governance.exception.GovernanceException;
import com.webank.weevent.governance.result.GovernanceResult;
import com.webank.weevent.governance.service.HistoricalDataService;

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
public class HistoricalDataController {

    @Autowired
    private HistoricalDataService historicalDataService;

    @PostMapping("/list")
    public GovernanceResult historicalDataList(@RequestBody TopicTopicHistoricalEntity topicHistoricalEntity, HttpServletRequest request,
                                               HttpServletResponse response) throws GovernanceException {
        log.info("get  historicalDataEntity:{} ", topicHistoricalEntity);
        Map<String, List<Integer>> returnMap = historicalDataService.historicalDataList(topicHistoricalEntity, request, response);
        return new GovernanceResult(returnMap);
    }

    @PostMapping("/eventList")
    public GovernanceResult eventList(@RequestBody TopicTopicHistoricalEntity topicHistoricalEntity, HttpServletRequest request,
                                      HttpServletResponse response) throws GovernanceException {
        log.info("get  eventList:{} ", topicHistoricalEntity);
        List<TopicTopicHistoricalEntity> topicTopicHistoricalEntities = historicalDataService.eventList(topicHistoricalEntity, request);
        return new GovernanceResult(topicTopicHistoricalEntities);
    }

}
