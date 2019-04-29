package com.webank.weevent.governance.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@CrossOrigin
@RestController
@Slf4j
public class ForwardController {
    
    @Autowired
    RestTemplate restTemplate;
    
    @Value("${weevent.url}")
    private String url;
    
    @RequestMapping(value = "/weevent/{path1}/{path2}", method =RequestMethod.GET)
    public Object forward(@PathVariable(name = "path1") String path1,
        @PathVariable(name = "path2") String path2) {
        log.info("weevent url: /wevent/"+ path1 + "/"+ path2);
        String forwarUrl = this.url + "/"+ path1 + "/" + path2;
        Object result = restTemplate.getForEntity(forwarUrl,Object.class).getBody();
        return result;
    }
    
    @RequestMapping(value = "/weevent/admin/deploy_topic_control", method =RequestMethod.GET)
    public Object forward() {
        log.info("wevent url: /weevent/admin/deploy_topic_control");
        String forwarUrl = this.url + "/admin/deploy_topic_control";
        String result = restTemplate.getForEntity(forwarUrl,String.class).getBody();
        return result;
    }
    
    @RequestMapping(value = "/weevent/{path1}/{path2}")
    public Object forward(@PathVariable(name = "path1") String path1,
        @PathVariable(name = "path2") String path2, 
        @RequestParam String topic) {
        log.info("wevent url: /weevent/"+ path1 + "/"+path2);
        
        String forwarUrl = this.url + "/"+ path1 + "/" + path2 + "?topic=" + topic;
        Object result = restTemplate.getForEntity(forwarUrl,Object.class).getBody();
        return result;
    }
}
