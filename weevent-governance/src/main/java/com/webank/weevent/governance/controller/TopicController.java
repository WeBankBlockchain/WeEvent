package com.webank.weevent.governance.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

import com.webank.weevent.governance.entity.Host;
import com.webank.weevent.governance.service.TopicService;

@CrossOrigin
@RestController
@RequestMapping(value = "topic")
@Slf4j
public class TopicController {

    @Autowired
    TopicService topicService;
    
    /**
     * just for test...
     * @return
     */
    @RequestMapping(value = "/hello", method = RequestMethod.GET)
    public String hello() {
        log.info("Hello World test...");
        return "Hello World!";
    }

    @RequestMapping(value = "/close")
    public Object close(@RequestParam("id") Integer id,@RequestParam String topic) {
        log.info("id:" + id +"close: "+ topic);
        return topicService.close(id,topic);
    }

    @RequestMapping(value = "/list")
    public Object getTopis(@RequestParam("id") Integer id,@RequestParam(name = "pageIndex") Integer pageIndex,
        @RequestParam(name = "pageSize") Integer pageSize) {
        log.info("pageIndex: " + pageIndex+ " pageSize: " + pageSize);
        return topicService.getTopics(id,pageIndex,pageSize);
    }
    
    @RequestMapping(value = "/openTopic")
    public Object open(@RequestParam("id") Integer id,@RequestParam String topic,@RequestParam String creater) {
        log.info("creater: "+creater+" open: "+topic);
        return topicService.open(id,topic,creater);
    }
    
}
