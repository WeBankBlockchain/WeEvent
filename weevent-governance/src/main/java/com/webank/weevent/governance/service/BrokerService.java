package com.webank.weevent.governance.service;

import java.util.List;
import javax.annotation.PostConstruct;

import com.webank.weevent.governance.code.ErrorCode;
import com.webank.weevent.governance.entity.Broker;
import com.webank.weevent.governance.entity.TopicPage;
import com.webank.weevent.governance.exception.GovernanceException;
import com.webank.weevent.governance.mapper.BrokerMapper;
import com.webank.weevent.governance.result.GovernanceResult;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * 
 * BrokerService
 * 
 * @since 2019/04/28
 *
 */
@Service
@Slf4j
public class BrokerService {

    @Autowired
    BrokerMapper brokerMapper;

    @Autowired
    ClientHttpRequestFactory factory;

    public List<Broker> getBrokers(Integer userId) {
        return brokerMapper.getBrokers(userId);
    }

    public Broker getBroker(Integer id) {
        return brokerMapper.getBroker(id);
    }

    public GovernanceResult addBroker(Broker broker) throws GovernanceException {
        // get brokerUrl
        String brokerUrl = broker.getBrokerUrl();
        // get restTemplate
        RestTemplate restTemplate = generateRestTemplate(brokerUrl);
        // get one of broker urls
        brokerUrl = brokerUrl + "/rest/list?pageIndex=0&pageSize=10";
        try {
            restTemplate.getForEntity(brokerUrl, TopicPage.class).getBody();
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new GovernanceException(ErrorCode.BROKER_CONNECT_ERROR);
        }

        // get webaseUrl
        String webaseUrl = broker.getWebaseUrl();
        // get restTemplate
        restTemplate = generateRestTemplate(webaseUrl);
        // get one of broker urls
        webaseUrl = webaseUrl + "/node/nodeInfo/1";
        try {
            restTemplate.getForEntity(webaseUrl, String.class);
        } catch (Exception e) {
            throw new GovernanceException(ErrorCode.WEBASE_CONNECT_ERROR);
        }

        brokerMapper.addBroker(broker);

        return GovernanceResult.ok(true);
    }

    public Boolean deleteBroker(Integer id) {
        return brokerMapper.deleteBroker(id);
    }

    public GovernanceResult updateBroker(Broker broker) throws GovernanceException {
        // get brokerUrl
        String brokerUrl = broker.getBrokerUrl();
        // get restTemplate
        RestTemplate restTemplate = generateRestTemplate(brokerUrl);
        // get one of broker urls
        brokerUrl = brokerUrl + "/rest/list?pageIndex=0&pageSize=10";
        try {
            restTemplate.getForEntity(brokerUrl, TopicPage.class).getBody();
        } catch (Exception e) {
            throw new GovernanceException(ErrorCode.BROKER_CONNECT_ERROR);
        }

        // get webaseUrl
        String webaseUrl = broker.getWebaseUrl();
        // get restTemplate
        restTemplate = generateRestTemplate(webaseUrl);
        // get one of broker urls
        webaseUrl = webaseUrl + "/node/nodeInfo/1";
        try {
            restTemplate.getForEntity(webaseUrl, String.class);
        } catch (Exception e) {
            throw new GovernanceException(ErrorCode.WEBASE_CONNECT_ERROR);
        }
        brokerMapper.updateBroker(broker);
        return GovernanceResult.ok(true);
    }

    // generate Restemplate from url
    private RestTemplate generateRestTemplate(String url) {
        RestTemplate restTemplate = null;
        if (url.startsWith("https")) {
            restTemplate = new RestTemplate(factory);
        } else {
            HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory();
            httpRequestFactory.setConnectionRequestTimeout(3000);
            httpRequestFactory.setConnectTimeout(3000);
            httpRequestFactory.setReadTimeout(3000);
            restTemplate = new RestTemplate(httpRequestFactory);
        }

        return restTemplate;
    }
}
