package com.webank.weevent.governance.service;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webank.weevent.governance.code.ErrorCode;
import com.webank.weevent.governance.entity.BrokerEntity;
import com.webank.weevent.governance.entity.PermissionEntity;
import com.webank.weevent.governance.enums.IsCreatorEnum;
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

    public List<BrokerEntity> getBrokers(HttpServletRequest request) {
        String accountId = cookiesTools.getCookieValueByName(request, ConstantProperties.COOKIE_MGR_ACCOUNT_ID);
        List<BrokerEntity> brokerEntityList = brokerMapper.getBrokers(Integer.parseInt(accountId));
        //Set the identity of the creation and authorization
        brokerEntityList.forEach(brokerEntity -> {
            if (accountId.equals(brokerEntity.getUserId().toString())) {
                brokerEntity.setIsCreator(IsCreatorEnum.CREATOR.getCode());
            } else {
                brokerEntity.setIsCreator(IsCreatorEnum.AUTHORIZED.getCode());
            }
        });
        return brokerEntityList;
    }

    public BrokerEntity getBroker(Integer id) {
        return brokerMapper.getBroker(id);
    }

    @Transactional(rollbackFor = Throwable.class)
    public GovernanceResult addBroker(BrokerEntity brokerEntity, HttpServletRequest request, HttpServletResponse response)
            throws GovernanceException {
        String accountId = cookiesTools.getCookieValueByName(request, ConstantProperties.COOKIE_MGR_ACCOUNT_ID);
        if (accountId == null || !accountId.equals(brokerEntity.getUserId().toString())) {
            throw new GovernanceException(ErrorCode.ACCESS_DENIED);
        }
        //checkBrokerUrl
        String brokerUrl = brokerEntity.getBrokerUrl();
        checkUrl(brokerUrl, brokerListUrl, request);
        //checkWeBaseUrl
        String webaseUrl = brokerEntity.getWebaseUrl();
        checkUrl(webaseUrl, weBaseNodeUrl, request);
        brokerMapper.addBroker(brokerEntity);
        //create permissionEntityList
        List<PermissionEntity> permissionEntityList = createPerMissionList(brokerEntity);
        if (permissionEntityList.size() > 0) {
            permissionMapper.batchInsert(permissionEntityList);
        }

        return GovernanceResult.ok(true);
    }

    private List<PermissionEntity> createPerMissionList(BrokerEntity brokerEntity) {
        log.info("brokerId: {}", brokerEntity.getId());
        List<PermissionEntity> permissionEntityList = new ArrayList<>();
        List<Integer> userIdList = brokerEntity.getUserIdList();
        if (userIdList == null) {
            return permissionEntityList;
        }
        userIdList.forEach(userId -> {
            PermissionEntity permissionEntity = new PermissionEntity();
            permissionEntityList.add(permissionEntity);
            permissionEntity.setUserId(userId);
            permissionEntity.setBrokerId(brokerEntity.getId());
        });
        return permissionEntityList;
    }

    private void checkUrl(String url, String afterUrl, HttpServletRequest request) throws GovernanceException {
        // get httpclient
        String headUrl = url;
        CloseableHttpClient client = generateHttpClient(headUrl);
        // get one of broker urls
        headUrl = headUrl + afterUrl;
        HttpGet get = getMethod(headUrl, request);
        try {
            client.execute(get);
        } catch (Exception e) {
            log.error("url {}, connect fail,error:{}", headUrl, e.getMessage());
            throw new GovernanceException("url " + headUrl + " connect fail", e);
        }
    }

    @Transactional(rollbackFor = Throwable.class)
    public GovernanceResult deleteBroker(BrokerEntity brokerEntity, HttpServletRequest request) throws GovernanceException {
        authCheck(brokerEntity, request);
        topicInfoMapper.deleteByBrokerId(brokerEntity.getId());
        brokerMapper.deleteBroker(brokerEntity.getId());
        permissionMapper.deletePermission(brokerEntity.getId());
        return GovernanceResult.ok(true);
    }

    @Transactional(rollbackFor = Throwable.class)
    public GovernanceResult updateBroker(BrokerEntity brokerEntity, HttpServletRequest request, HttpServletResponse response)
            throws GovernanceException {
        authCheck(brokerEntity, request);

        //checkBrokerUrl
        String brokerUrl = brokerEntity.getBrokerUrl();
        checkUrl(brokerUrl, brokerListUrl, request);
        //checkWeBaseUrl
        String webaseUrl = brokerEntity.getWebaseUrl();
        checkUrl(webaseUrl, weBaseNodeUrl, request);
        brokerMapper.updateBroker(brokerEntity);
        //delete old permission
        permissionMapper.deletePermission(brokerEntity.getId());
        //create new permission
        List<PermissionEntity> perMissionList = createPerMissionList(brokerEntity);
        if (perMissionList.size() > 0) {
            permissionMapper.batchInsert(perMissionList);
        }
        return GovernanceResult.ok(true);
    }

    private void authCheck(BrokerEntity brokerEntity, HttpServletRequest request) throws GovernanceException {
        String accountId = cookiesTools.getCookieValueByName(request, ConstantProperties.COOKIE_MGR_ACCOUNT_ID);
        BrokerEntity oldBrokerEntity = brokerMapper.getBroker(brokerEntity.getId());
        if (!accountId.equals(oldBrokerEntity.getUserId().toString())) {
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
