package com.webank.weevent.governance.service;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import lombok.extern.slf4j.Slf4j;

import com.webank.weevent.governance.entity.Broker;
import com.webank.weevent.governance.entity.Topic;
import com.webank.weevent.governance.entity.TopicPage;
import com.webank.weevent.governance.mapper.TopicInfoMapper;

/**
 * topic service
 * 
 * @since 2018/12/18
 */
@Service
@Slf4j
public class TopicService {

	private RestTemplate restTemplate;

	@Autowired
	TopicInfoMapper topicInfoMapper;

	@Autowired
	BrokerService brokerService;

	@Autowired
	ClientHttpRequestFactory factory;

	public Boolean close(Integer brokerId, String topic) {
		// get broker
		Broker broker = brokerService.getBroker(brokerId);
		if (broker != null) {
			generateRestTemplate(broker.getBrokerUrl());
			String url = broker.getBrokerUrl() + "/weevent/rest/close?topic=" + topic;
			log.info("url: " + url);
			Boolean response = restTemplate.getForEntity(url, Boolean.class).getBody();
			return response;
		}
		return false;
	}

	public TopicPage getTopics(Integer brokerId, Integer pageIndex, Integer pageSize) {
		// getBroker
		Broker broker = brokerService.getBroker(brokerId);

		generateRestTemplate(broker.getBrokerUrl());

		// get eventbroker url
		String url = broker.getBrokerUrl() + "/weevent/rest/list";
		url = url + "?pageIndex=" + pageIndex + "&pageSize=" + pageSize;
		log.info(url);
		TopicPage result = restTemplate.getForEntity(url, TopicPage.class).getBody();
		log.info("result json=" + result);
		if (result != null) {
			List<Topic> topicList = null;
			TopicPage topicPage = null;

			topicList = result.getTopicInfoList();
			//get creater from database
			if (topicList.size() > 0) {
				for (int i = 0; i < topicList.size(); i++) {
					String creater = topicInfoMapper.getCreater(brokerId, topicList.get(i).getTopicName());
					if (!StringUtils.isEmpty(creater)) {
						topicList.get(i).setCreater(creater);
					}
				}
			}
			result.setTopicInfoList(topicList);
			return topicPage;
		}
		return null;
	}

	@Transactional
	public Boolean open(Integer brokerId, String topic, String creater) {
		// get broker
		Broker broker = brokerService.getBroker(brokerId);
		if (broker != null) {
			topicInfoMapper.openBrokeTopic(brokerId, topic, creater);
			generateRestTemplate(broker.getBrokerUrl());
			String url = broker.getBrokerUrl() + "/weevent/rest/open?topic=" + topic;
			log.info("topic: " + topic + " creater: " + creater);
			Boolean response = restTemplate.getForEntity(url, Boolean.class).getBody();
			return response;
		}
		return false;
	}

	// generate Restemplate from url
	private void generateRestTemplate(String url) {
		if (url.startsWith("https")) {
			restTemplate = new RestTemplate(factory);
		} else {
			restTemplate = new RestTemplate();
		}
	}

}
