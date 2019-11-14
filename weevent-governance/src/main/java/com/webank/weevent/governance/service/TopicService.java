package com.webank.weevent.governance.service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webank.weevent.governance.code.ErrorCode;
import com.webank.weevent.governance.entity.BrokerEntity;
import com.webank.weevent.governance.entity.TopicEntity;
import com.webank.weevent.governance.entity.TopicPage;
import com.webank.weevent.governance.entity.TopicPageEntity;
import com.webank.weevent.governance.exception.GovernanceException;
import com.webank.weevent.governance.mapper.TopicInfoMapper;
import com.webank.weevent.governance.properties.ConstantProperties;
import com.webank.weevent.governance.result.GovernanceResult;
import com.webank.weevent.governance.utils.CookiesTools;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * topic service
 *
 * @since 2018/12/18
 */
@Service
@Slf4j
public class TopicService {

    @Autowired
    private TopicInfoMapper topicInfoMapper;

    @Autowired
    private BrokerService brokerService;

    @Autowired
    private CookiesTools cookiesTools;

    @Autowired
    private CommonService commonService;

    @Autowired
    private PermissionService permissionService;

    private final String SPLIT = "-";

    public Boolean close(Integer brokerId, String topic, String groupId, HttpServletRequest request, HttpServletResponse response)
            throws GovernanceException {
        String accountId = cookiesTools.getCookieValueByName(request, ConstantProperties.COOKIE_MGR_ACCOUNT_ID);
        BrokerEntity brokerEntity = brokerService.getBroker(brokerId);
        if (brokerEntity == null) {
            return false;
        }
        Boolean flag = permissionService.verifyPermissions(brokerId, accountId);
        if (!flag) {
            throw new GovernanceException(ErrorCode.ACCESS_DENIED);
        }
        CloseableHttpClient client = commonService.generateHttpClient(brokerEntity.getBrokerUrl());
        String url;
        try {
            if (groupId == null) {
                url = new StringBuffer(brokerEntity.getBrokerUrl()).append(ConstantProperties.BROKER_REST_CLOSE).append("?topic=").append(URLEncoder.encode(topic, "UTF-8")).
                        toString();
            } else {
                url = new StringBuffer(brokerEntity.getBrokerUrl()).append(ConstantProperties.BROKER_REST_CLOSE).append("?topic=").append(URLEncoder.encode(topic, "UTF-8"))
                        .append("&groupId=").append(groupId).toString();
            }
            log.info("url:{}", url);
            HttpGet get = commonService.getMethod(url, request);
            CloseableHttpResponse closeResponse = client.execute(get);
            String mes = EntityUtils.toString(closeResponse.getEntity());
            return (Boolean) JSON.parse(mes);
        } catch (Exception e) {
            log.error("close topic fail,topic :{},error:{}", topic, e.getMessage());
            throw new GovernanceException(ErrorCode.BROKER_CONNECT_ERROR);
        }
    }

    public TopicPage getTopics(TopicPageEntity topicPageEntity, HttpServletRequest request,
                               HttpServletResponse response) throws GovernanceException {
        Integer brokerId = topicPageEntity.getBrokerId();
        Integer pageIndex = topicPageEntity.getPageIndex();
        Integer pageSize = topicPageEntity.getPageSize();
        String groupId = topicPageEntity.getGroupId();
        String accountId = cookiesTools.getCookieValueByName(request, ConstantProperties.COOKIE_MGR_ACCOUNT_ID);
        BrokerEntity brokerEntity = brokerService.getBroker(brokerId);
        Boolean flag = permissionService.verifyPermissions(brokerId, accountId);
        if (!flag) {
            throw new GovernanceException(ErrorCode.ACCESS_DENIED);
        }
        TopicPage result = new TopicPage();
        result.setPageIndex(pageIndex);
        result.setPageSize(pageSize);
        if (brokerEntity == null) {
            return result;
        }
        // get event broker url
        CloseableHttpClient client = commonService.generateHttpClient(brokerEntity.getBrokerUrl());
        String url;
        if (groupId != null) {
            url = new StringBuffer(brokerEntity.getBrokerUrl()).append(ConstantProperties.BROKER_REST_LIST)
                    .append("?pageIndex=").append(pageIndex).append("&pageSize=").append(pageSize)
                    .append("&groupId=").append(groupId).toString();
        } else {
            url = new StringBuffer(brokerEntity.getBrokerUrl()).append(ConstantProperties.BROKER_REST_LIST)
                    .append("?pageIndex=").append(pageIndex).append("&pageSize=").append(pageSize).toString();
        }
        log.info("url: {}", url);

        HttpGet get = commonService.getMethod(url, request);
        try {
            CloseableHttpResponse closeResponse = client.execute(get);
            String mes = EntityUtils.toString(closeResponse.getEntity());
            JSON json = JSON.parseObject(mes);
            result = JSON.toJavaObject(json, TopicPage.class);
            if (result == null || CollectionUtils.isEmpty(result.getTopicInfoList())) {
                return result;
            }
            //get creator
            List<TopicEntity> topicEntityList = result.getTopicInfoList();
            List<String> topicNameList = new ArrayList<>();
            topicEntityList.forEach(it -> {
                topicNameList.add(it.getTopicName());
            });
            List<TopicEntity> topicEntities = topicInfoMapper.getCreator(brokerId, groupId, topicNameList);
            if (CollectionUtils.isEmpty(topicEntities)) {
                return result;
            }
            Map<String, String> creatorMap = new HashMap<>();
            topicEntities.forEach(it -> {
                creatorMap.put(getKey(brokerId, groupId, it.getTopicName()), it.getCreater());
            });
            // set creator
            topicEntityList.forEach(it -> {
                it.setCreater(creatorMap.get(getKey(brokerId, groupId, it.getTopicName())));
            });
            result.setTopicInfoList(topicEntityList);
            return result;
        } catch (Exception e) {
            log.error("get topics fail,brokerId :{},error:{}", brokerId, e.getMessage());
            throw new GovernanceException("get topics fail", e);
        }
    }

    public TopicEntity getTopicInfo(Integer brokerId, String topic, String groupId, HttpServletRequest request) throws GovernanceException {
        String accountId = this.cookiesTools.getCookieValueByName(request, ConstantProperties.COOKIE_MGR_ACCOUNT_ID);
        BrokerEntity broker = this.brokerService.getBroker(brokerId);
        Boolean flag = permissionService.verifyPermissions(brokerId, accountId);
        if (broker == null || !flag) {
            log.error("get topicInfo failed, brokerId:{}, topic:{}, groupId:{}.", brokerId, topic, groupId);
            throw new GovernanceException(ErrorCode.ACCESS_DENIED);
        }

        try {
            CloseableHttpClient client = commonService.generateHttpClient(broker.getBrokerUrl());
            // get event broker url
            String url = new StringBuffer(broker.getBrokerUrl()).append(ConstantProperties.BROKER_REST_STATE).append("?topic=")
                    .append(URLEncoder.encode(topic, "UTF-8")).toString();
            if (!StringUtils.isBlank(groupId)) {
                url = new StringBuffer(url).append("&groupId=").append(groupId).toString();
            }

            log.info("getTopicInfo url:{}", url);
            HttpGet get = commonService.getMethod(url, request);
            CloseableHttpResponse closeResponse = client.execute(get);
            String mes = EntityUtils.toString(closeResponse.getEntity());
            JSON json = JSON.parseObject(mes);
            TopicEntity result = JSON.toJavaObject(json, TopicEntity.class);

            if (result != null) {
                // get creator from database
                List<TopicEntity> creators = this.topicInfoMapper.getCreator(brokerId, groupId, new ArrayList<>(Collections.singletonList(topic)));
                if (CollectionUtils.isNotEmpty(creators)) {
                    result.setCreater(creators.get(0).getCreater());
                }
                return result;
            }
        } catch (Exception e) {
            log.error("get topicInfo failed, error:{}", e.getMessage());
            throw new GovernanceException(ErrorCode.BROKER_CONNECT_ERROR);
        }
        return null;
    }

    @Transactional(rollbackFor = Throwable.class)
    public GovernanceResult open(Integer brokerId, String topic, String creater, String groupId, HttpServletRequest request,
                                 HttpServletResponse response) throws GovernanceException {
        String accountId = cookiesTools.getCookieValueByName(request, ConstantProperties.COOKIE_MGR_ACCOUNT_ID);
        BrokerEntity brokerEntity = brokerService.getBroker(brokerId);
        if (brokerEntity == null) {
            return null;
        }
        Boolean flag = permissionService.verifyPermissions(brokerId, accountId);
        if (!flag) {
            throw new GovernanceException(ErrorCode.ACCESS_DENIED);
        }
        String mes;
        try {
            boolean exist = exist(topic, brokerEntity.getBrokerUrl(), groupId, request);
            if (exist) {
                log.info("topic already exists,topic{}", topic);
                return new GovernanceResult(ErrorCode.TOPIC_EXISTS);
            }
            TopicEntity topicEntity = new TopicEntity();
            topicEntity.setBrokerId(brokerId);
            topicEntity.setTopicName(topic);
            topicEntity.setCreater(creater);
            topicEntity.setGroupId(groupId);
            topicInfoMapper.openBrokeTopic(topicEntity);

            CloseableHttpClient client = commonService.generateHttpClient(brokerEntity.getBrokerUrl());
            String url;
            if (groupId != null) {
                url = new StringBuffer(brokerEntity.getBrokerUrl()).append(ConstantProperties.BROKER_REST_OPEN).append("?topic=").append(URLEncoder.encode(topic, "UTF-8"))
                        .append("&groupId=").append(groupId).toString();
            } else {
                url = new StringBuffer(brokerEntity.getBrokerUrl()).append(ConstantProperties.BROKER_REST_OPEN).append("?topic=").append(URLEncoder.encode(topic, "UTF-8")).toString();
            }
            log.info("url: {}", url);
            HttpGet get = commonService.getMethod(url, request);
            CloseableHttpResponse closeResponse = client.execute(get);
            mes = EntityUtils.toString(closeResponse.getEntity());
        } catch (Exception e) {
            log.error("broker connect error,error:{}", e.getMessage());
            throw new GovernanceException(ErrorCode.BROKER_CONNECT_ERROR);
        }
        try {
            Boolean result = (Boolean) JSON.parse(mes);
            return new GovernanceResult(result);
        } catch (Exception e) {
            log.error("parse json fail,error:{}", e.getMessage());
            JSON json = JSON.parseObject(mes);
            @SuppressWarnings("unchecked")
            Map<String, Object> result = JSON.toJavaObject(json, Map.class);
            throw new GovernanceException((Integer) (result.get("code")), result.get("message").toString());
        }
    }

    @Transactional(rollbackFor = Throwable.class)
    public boolean exist(String topic, String brokerUrl, String groupId, HttpServletRequest request) throws GovernanceException, UnsupportedEncodingException {
        String url = "";
        if (groupId == null) {
            url = new StringBuffer(brokerUrl).append(ConstantProperties.BROKER_REST_EXIST).append("?topic=").append(URLEncoder.encode(topic, "UTF-8"))
                    .toString();
        } else {
            url = new StringBuffer(brokerUrl).append(ConstantProperties.BROKER_REST_EXIST).append("?topic=").append(URLEncoder.encode(topic, "UTF-8"))
                    .append("&groupId=").append(groupId).toString();
        }

        log.info("url: {}", url);
        String mes;
        try {
            CloseableHttpClient client = commonService.generateHttpClient(brokerUrl);
            HttpGet get = commonService.getMethod(url, request);
            CloseableHttpResponse closeResponse = client.execute(get);
            mes = EntityUtils.toString(closeResponse.getEntity());
        } catch (Exception e) {
            log.error("broker connect error,error:{}", e.getMessage());
            throw new GovernanceException(ErrorCode.BROKER_CONNECT_ERROR);
        }
        try {
            return (Boolean) JSON.parse(mes);
        } catch (Exception e) {
            log.error("parse json fail,error:{}", e.getMessage());
            JSON json = JSON.parseObject(mes);
            @SuppressWarnings("unchecked")
            Map<String, Object> result = JSON.toJavaObject(json, Map.class);
            throw new GovernanceException((Integer) (result.get("code")), result.get("message").toString());
        }
    }


    private String getKey(Integer brokerId, String groupId, String topicName) {
        return new StringBuilder(brokerId).append(SPLIT).append(topicName).append(SPLIT).append(groupId).toString();
    }

}
