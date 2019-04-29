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

    @RequestMapping(value = "/getTopics")
    public Object getTopis(@RequestParam(name = "pageIndex") Integer pageIndex,
        @RequestParam(name = "pageSize") Integer pageSize) {
        log.info("pageIndex: " + pageIndex+ " pageSize: " + pageSize);
        return topicService.getTopics(pageIndex,pageSize);
    }

    /**
     * just for test...
     * @return
     */
    @RequestMapping(value = "/hello", method = RequestMethod.GET)
    public String hello() {
        log.info("Hello World test...");
        return "Hello World!";
    }

    @RequestMapping(value = "/open")
    public Object open(@RequestParam String topic,@RequestParam String creater) {
        log.info("creater: "+creater+" open: "+topic);
        return topicService.open(topic,creater);
    }
    
    @RequestMapping(value = "/close")
    public Boolean close(@RequestParam String topic) {
        log.info("close: "+topic);
        return topicService.close(topic);
    }

    @RequestMapping(value = "/getHost")
    public List<Host> getHost() {
        log.info("get Host.....");
        return topicService.getHost();
    }
}
