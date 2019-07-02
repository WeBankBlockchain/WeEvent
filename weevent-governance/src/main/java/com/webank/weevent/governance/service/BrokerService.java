package com.webank.weevent.governance.service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webank.weevent.governance.code.ErrorCode;
import com.webank.weevent.governance.entity.Broker;
import com.webank.weevent.governance.exception.GovernanceException;
import com.webank.weevent.governance.mapper.BrokerMapper;
import com.webank.weevent.governance.properties.ConstantProperties;
import com.webank.weevent.governance.result.GovernanceResult;
import com.webank.weevent.governance.utils.CookiesTools;
import com.webank.weevent.governance.utils.SpringContextUtil;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

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
    private CookiesTools cookiesTools;

    public List<Broker> getBrokers(HttpServletRequest request) {
        HttpServletRequest req = (HttpServletRequest) request;
        String accountId = cookiesTools.getCookieValueByName(req, ConstantProperties.COOKIE_MGR_ACCOUNT_ID);
        return brokerMapper.getBrokers(Integer.parseInt(accountId));
    }

    public Broker getBroker(Integer id) {
        return brokerMapper.getBroker(id);
    }

    public GovernanceResult addBroker(Broker broker, HttpServletRequest request, HttpServletResponse response)
            throws GovernanceException {
        HttpServletRequest req = (HttpServletRequest) request;

        String accountId = cookiesTools.getCookieValueByName(req, ConstantProperties.COOKIE_MGR_ACCOUNT_ID);
        if (!accountId.equals(broker.getUserId().toString())) {
            throw new GovernanceException(ErrorCode.ACCESS_DENIED);
        }

        // get brokerUrl
        String brokerUrl = broker.getBrokerUrl();
        // get httpclient
        CloseableHttpClient client = generateHttpClient(brokerUrl);
        // get one of broker urls
        brokerUrl = brokerUrl + "/rest/list?pageIndex=0&pageSize=10";
        HttpGet get = getMethod(brokerUrl, request);
        try {
            client.execute(get);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new GovernanceException(ErrorCode.BROKER_CONNECT_ERROR);
        }

        // get webaseUrl
        String webaseUrl = broker.getWebaseUrl();
        // get restTemplate
        client = generateHttpClient(webaseUrl);
        get = getMethod(webaseUrl, request);
        // get one of broker urls
        webaseUrl = webaseUrl + "/node/nodeInfo/1";
        try {
            client.execute(get);
        } catch (Exception e) {
            throw new GovernanceException(ErrorCode.WEBASE_CONNECT_ERROR);
        }

        brokerMapper.addBroker(broker);

        return GovernanceResult.ok(true);
    }

    public Boolean deleteBroker(Integer id) {
        return brokerMapper.deleteBroker(id);
    }

    public GovernanceResult updateBroker(Broker broker, HttpServletRequest request, HttpServletResponse response)
            throws GovernanceException {
        HttpServletRequest req = (HttpServletRequest) request;

        String accountId = cookiesTools.getCookieValueByName(req, ConstantProperties.COOKIE_MGR_ACCOUNT_ID);
        Broker oldBroker = brokerMapper.getBroker(broker.getId());
        if (!accountId.equals(oldBroker.getUserId().toString())) {
            throw new GovernanceException(ErrorCode.ACCESS_DENIED);
        }

        // get brokerUrl
        String brokerUrl = broker.getBrokerUrl();
        // get httpclient
        CloseableHttpClient client = generateHttpClient(brokerUrl);
        // get one of broker urls
        brokerUrl = brokerUrl + "/rest/list?pageIndex=0&pageSize=10";
        HttpGet get = getMethod(brokerUrl, request);
        try {
            client.execute(get);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new GovernanceException(ErrorCode.BROKER_CONNECT_ERROR);
        }

        // get webaseUrl
        String webaseUrl = broker.getWebaseUrl();
        // get restTemplate
        client = generateHttpClient(webaseUrl);
        get = getMethod(webaseUrl, request);
        // get one of broker urls
        webaseUrl = webaseUrl + "/node/nodeInfo/1";
        try {
            client.execute(get);
        } catch (Exception e) {
            throw new GovernanceException(ErrorCode.WEBASE_CONNECT_ERROR);
        }
        
        brokerMapper.updateBroker(broker);
        return GovernanceResult.ok(true);
    }

    // generate CloseableHttpClient from url
    private CloseableHttpClient generateHttpClient(String url) {
        if (url.startsWith("https")) {
            CloseableHttpClient bean = (CloseableHttpClient) SpringContextUtil.getBean("httpsClient");
            return bean;
        } else {
            CloseableHttpClient bean = (CloseableHttpClient) SpringContextUtil.getBean("httpClient");
            return bean;
        }
    }

    private HttpGet getMethod(String uri, HttpServletRequest request) {
        try {
            URIBuilder builder = new URIBuilder(uri);
            Enumeration<String> enumeration = request.getParameterNames();
            while (enumeration.hasMoreElements()) {
                String nex = enumeration.nextElement();
                builder.setParameter(nex, request.getParameter(nex));
            }
            return new HttpGet(builder.build());
        } catch (URISyntaxException e) {
            log.error(e.getMessage());
        }
        return null;
    }
}
