package com.webank.weevent.governance.service;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webank.weevent.governance.code.ErrorCode;
import com.webank.weevent.governance.entity.Broker;
import com.webank.weevent.governance.entity.Permission;
import com.webank.weevent.governance.exception.GovernanceException;
import com.webank.weevent.governance.mapper.BrokerMapper;
import com.webank.weevent.governance.mapper.PermissionMapper;
import com.webank.weevent.governance.mapper.TopicInfoMapper;
import com.webank.weevent.governance.properties.ConstantProperties;
import com.webank.weevent.governance.result.GovernanceResult;
import com.webank.weevent.governance.utils.CookiesTools;
import com.webank.weevent.governance.utils.SpringContextUtil;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * BrokerService
 *
 * @since 2019/04/28
 */
@Service
@Transactional(rollbackFor = Throwable.class)
@Slf4j
public class BrokerService {

    @Autowired
    private BrokerMapper brokerMapper;

    @Autowired
    private TopicInfoMapper topicInfoMapper;

    @Autowired
    private PermissionMapper permissionMapper;

    private final static String brokerListUrl = "/rest/list?pageIndex=0&pageSize=10";

    private final static String weBaseNodeUrl = "/node/nodeInfo/1";

    @Autowired
    private CookiesTools cookiesTools;

    public List<Broker> getBrokers(HttpServletRequest request) {
        String accountId = cookiesTools.getCookieValueByName(request, ConstantProperties.COOKIE_MGR_ACCOUNT_ID);
        return brokerMapper.getBrokers(Integer.parseInt(accountId));
    }

    public Broker getBroker(Integer id) {
        return brokerMapper.getBroker(id);
    }

    public GovernanceResult addBroker(Broker broker, HttpServletRequest request, HttpServletResponse response)
            throws GovernanceException {
        String accountId = cookiesTools.getCookieValueByName(request, ConstantProperties.COOKIE_MGR_ACCOUNT_ID);
        if (accountId == null || !accountId.equals(broker.getUserId().toString())) {
            throw new GovernanceException(ErrorCode.ACCESS_DENIED);
        }
        //checkBrokerUrl
        String brokerUrl = broker.getBrokerUrl();
        checkUrl(brokerUrl, brokerListUrl, request);
        //checkWeBaseUrl
        String webaseUrl = broker.getWebaseUrl();
        checkUrl(webaseUrl, weBaseNodeUrl, request);
        broker.setLastUpdate(new Date());
        brokerMapper.addBroker(broker);
        //create permissionList
        List<Permission> permissionList = createPerMissionList(broker);
        if (permissionList.size() > 0) {
            permissionMapper.batchInsert(permissionList);
        }

        return GovernanceResult.ok(true);
    }

    private List<Permission> createPerMissionList(Broker broker) {
        List<Permission> permissionList = new ArrayList<>();
        List<Integer> userIdList = broker.getUserIdList();
        if (!userIdList.isEmpty()) {
            userIdList.forEach(userId -> {
                Permission permission = new Permission();
                permissionList.add(permission);
                permission.setUserId(userId);
                permission.setBrokerId(broker.getId());
            });
        }
        return permissionList;
    }

    private void checkUrl(String url, String afterUrl, HttpServletRequest request) throws GovernanceException {
        // get httpclient
        CloseableHttpClient client = generateHttpClient(url);
        // get one of broker urls
        url = url + afterUrl;
        HttpGet get = getMethod(url, request);
        try {
            client.execute(get);
        } catch (Exception e) {
            log.error("url {}, connect fail,error:{}", url, e.getMessage());
            throw new GovernanceException("url " + url + " connect fail", e);
        }
    }

    public GovernanceResult deleteBroker(Broker broker, HttpServletRequest request) throws GovernanceException {
        authCheck(broker, request);
        topicInfoMapper.deleteTopicInfo(broker.getId());
        brokerMapper.deleteBroker(broker.getId());
        permissionMapper.deletePermission(broker.getId());
        return GovernanceResult.ok(true);
    }

    public GovernanceResult updateBroker(Broker broker, HttpServletRequest request, HttpServletResponse response)
            throws GovernanceException {
        authCheck(broker, request);

        //checkBrokerUrl
        String brokerUrl = broker.getBrokerUrl();
        checkUrl(brokerUrl, brokerListUrl, request);
        //checkWeBaseUrl
        String webaseUrl = broker.getWebaseUrl();
        checkUrl(webaseUrl, weBaseNodeUrl, request);
        brokerMapper.updateBroker(broker);
        //delete old permission
        permissionMapper.deletePermission(broker.getId());
        //create new permission
        List<Permission> perMissionList = createPerMissionList(broker);
        if (perMissionList.size() > 0) {
            permissionMapper.batchInsert(perMissionList);
        }
        return GovernanceResult.ok(true);
    }

    private void authCheck(Broker broker, HttpServletRequest request) throws GovernanceException {
        String accountId = cookiesTools.getCookieValueByName(request, ConstantProperties.COOKIE_MGR_ACCOUNT_ID);
        Broker oldBroker = brokerMapper.getBroker(broker.getId());
        if (!accountId.equals(oldBroker.getUserId().toString())) {
            throw new GovernanceException(ErrorCode.ACCESS_DENIED);
        }
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
