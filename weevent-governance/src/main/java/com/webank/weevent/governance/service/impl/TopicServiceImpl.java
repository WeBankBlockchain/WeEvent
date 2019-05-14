package com.webank.weevent.governance.service.impl;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import com.alibaba.fastjson.JSONArray;
import lombok.extern.slf4j.Slf4j;
import retrofit2.HttpException;
import retrofit2.Response;

import com.webank.weevent.governance.entity.Broker;
import com.webank.weevent.governance.entity.Topic;
import com.webank.weevent.governance.entity.TopicPage;
import com.webank.weevent.governance.mapper.TopicInfoMapper;
import com.webank.weevent.governance.service.BrokerService;
import com.webank.weevent.governance.service.TopicService;

/**
 * topic service
 * @since 2018/12/18
 */
@Service
@Slf4j
public class TopicServiceImpl implements TopicService{

	private RestTemplate restTemplate;
	
    @Autowired
    TopicInfoMapper topicInfoMapper;
    
    @Autowired
    BrokerService brokerService;
    
    @Autowired
    ClientHttpRequestFactory factory;
    
    @Override
    public Object close(Integer id,String topic) {
    	//get broker
        Broker broker =  brokerService.getBroker(id);
        if(broker != null) {
        	generateRestTemplate(broker.getBrokerUrl());
        	String url = broker.getBrokerUrl() + "/weevent/rest/close?topic=" + topic;
            log.info("url: " + url);
            Object response = restTemplate.getForEntity(url, Object.class).getBody();
            return response;
        }
        return false;
    }


	@Override
	public Object getTopics(Integer id, Integer pageIndex, Integer pageSize) {
		Broker broker =  brokerService.getBroker(id);
		
		generateRestTemplate(broker.getBrokerUrl());
		
		 //get eventbroker url
        String url = broker.getBrokerUrl() + "/weevent/rest/list";
        url = url + "?pageIndex=" + pageIndex+"&pageSize="+pageSize;
        log.info(url);
        String json = restTemplate.getForEntity(url, String.class).getBody();
        log.info("result json="+json);
        if(json != null) {
            List<Topic> topicList = null;
            TopicPage topicPage = null;
            try {
                topicPage = JSONArray.parseObject(json,TopicPage.class);
                topicList = topicPage.getTopicInfoList();
                if(topicList.size() > 0) {
                    for (int i =0;i < topicList.size();i++) {
                        String creater = topicInfoMapper.getCreater(id,topicList.get(i).getTopicName());
                        if(!StringUtils.isEmpty(creater)) {
                            topicList.get(i).setCreater(creater);
                        }
                    }
                }
                
                topicPage.setTopicInfoList(topicList);
                
                return topicPage;
            } catch (Exception e) {
                return JSONArray.parseObject(json);
            }
            
        }
        return null;
	}
	
	@Override
	@Transactional
    public Object open(Integer id,String topic,String creater) {
		//get broker
        Broker broker =  brokerService.getBroker(id);
        if(broker != null) {
        	topicInfoMapper.openBrokeTopic(id,topic,creater);
        	generateRestTemplate(broker.getBrokerUrl());
        	String url = broker.getBrokerUrl() + "/weevent/rest/open?topic=" +topic;
            log.info("topic: "+ topic +" creater: " + creater);
            Object response = restTemplate.getForEntity(url, Object.class).getBody();
            if((response instanceof Boolean) && (Boolean)response) {
                return response;
            }else {
            	throw new HttpException((Response<?>) response);
            }
        }
        return null;
    }
	
	//generate Restemplate from url
	private void generateRestTemplate(String url) {
		if(url.startsWith("https")) {
			restTemplate = new RestTemplate(factory);
		}else {
			restTemplate = new RestTemplate();
		}
	}
	
}
