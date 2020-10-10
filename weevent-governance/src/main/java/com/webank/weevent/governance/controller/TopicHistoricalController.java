package com.webank.weevent.governance.controller;


import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webank.weevent.governance.common.GovernanceException;
import com.webank.weevent.governance.common.GovernanceResponse;
import com.webank.weevent.governance.entity.TopicHistoricalEntity;
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
    public GovernanceResponse<Map<String, List<Integer>>> historicalDataList(@RequestBody TopicHistoricalEntity topicHistoricalEntity, HttpServletRequest request,
                                               HttpServletResponse response) throws GovernanceException {
        log.info("get  historicalDataEntity:{} ", topicHistoricalEntity);
        Map<String, List<Integer>> returnMap = topicHistoricalService.historicalDataList(topicHistoricalEntity, request, response);
        return new GovernanceResponse<>(returnMap);
    }

    @PostMapping("/eventList")
    public GovernanceResponse<List<TopicHistoricalEntity>> eventList(@RequestBody TopicHistoricalEntity topicHistoricalEntity, HttpServletRequest request,
                                      HttpServletResponse response) throws GovernanceException {
        log.info("get  eventList:{} ", topicHistoricalEntity);
        List<TopicHistoricalEntity> topicTopicHistoricalEntities = topicHistoricalService.eventList(topicHistoricalEntity, request);
        return new GovernanceResponse<>(topicTopicHistoricalEntities);
    }

    @PostMapping("/insertHistoricalData")
    public GovernanceResponse<Boolean> insertHistoricalData(@RequestBody TopicHistoricalEntity topicHistoricalEntity) {
        log.info("insert  historicalData:{} ", topicHistoricalEntity);
        return new GovernanceResponse<>(topicHistoricalService.insertHistoricalData(topicHistoricalEntity));
    }

}
