package com.webank.weevent.governance.service;

import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webank.weevent.governance.code.ErrorCode;
import com.webank.weevent.governance.entity.BrokerEntity;
import com.webank.weevent.governance.entity.TopicEntity;
import com.webank.weevent.governance.entity.TopicPage;
import com.webank.weevent.governance.exception.GovernanceException;
import com.webank.weevent.governance.mapper.TopicInfoMapper;
import com.webank.weevent.governance.properties.ConstantProperties;
import com.webank.weevent.governance.result.GovernanceResult;
import com.webank.weevent.governance.utils.CookiesTools;
import com.webank.weevent.governance.utils.SpringContextUtil;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

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

    public Boolean close(Integer brokerId, String topic, HttpServletRequest request, HttpServletResponse response)
            throws GovernanceException {
        HttpServletRequest req = (HttpServletRequest) request;

        String accountId = cookiesTools.getCookieValueByName(req, ConstantProperties.COOKIE_MGR_ACCOUNT_ID);
        BrokerEntity brokerEntity = brokerService.getBroker(brokerId);
        if (brokerEntity != null) {
            if (!accountId.equals(brokerEntity.getUserId().toString())) {
                throw new GovernanceException(ErrorCode.ACCESS_DENIED);
            }
            CloseableHttpClient client = generateHttpClient(brokerEntity.getBrokerUrl());
            String url = brokerEntity.getBrokerUrl() + "/rest/close?topic=" + topic;
            log.info("url: " + url);
            HttpGet get = getMethod(url, request);

            try {
                CloseableHttpResponse closeResponse = client.execute(get);
                String mes = EntityUtils.toString(closeResponse.getEntity());
                return (Boolean) JSON.parse(mes);
            } catch (Exception e) {
                throw new GovernanceException(ErrorCode.BROKER_CONNECT_ERROR);
            }
        }
        return false;
    }

    public TopicPage getTopics(Integer brokerId, Integer pageIndex, Integer pageSize, HttpServletRequest request,
            HttpServletResponse response) throws GovernanceException {
        HttpServletRequest req = (HttpServletRequest) request;

        String accountId = cookiesTools.getCookieValueByName(req, ConstantProperties.COOKIE_MGR_ACCOUNT_ID);
        BrokerEntity brokerEntity = brokerService.getBroker(brokerId);
        if (!accountId.equals(brokerEntity.getUserId().toString())) {
            throw new GovernanceException(ErrorCode.ACCESS_DENIED);
        }

        if (brokerEntity != null) {

            CloseableHttpClient client = generateHttpClient(brokerEntity.getBrokerUrl());
            // get eventbroker url
            String url = brokerEntity.getBrokerUrl() + "/rest/list";
            url = url + "?pageIndex=" + pageIndex + "&pageSize=" + pageSize;

            log.info("url: " + url);
            HttpGet get = getMethod(url, request);

            try {
                CloseableHttpResponse closeResponse = client.execute(get);
                String mes = EntityUtils.toString(closeResponse.getEntity());
                log.info("result json: " + mes);
                JSON json = JSON.parseObject(mes);
                TopicPage result = JSON.toJavaObject(json, TopicPage.class);

                if (result != null) {
                    List<TopicEntity> topicEntityList = null;

                    topicEntityList = result.getTopicEntityInfoList();
                    // get creater from database
                    if (topicEntityList.size() > 0) {
                        for (int i = 0; i < topicEntityList.size(); i++) {
                            String creater = topicInfoMapper.getCreater(brokerId, topicEntityList.get(i).getTopicName());
                            if (!StringUtils.isEmpty(creater)) {
                                topicEntityList.get(i).setCreater(creater);
                            }
                        }
                    }
                    result.setTopicEntityInfoList(topicEntityList);
                    return result;
                }
            } catch (Exception e) {
                throw new GovernanceException(ErrorCode.BROKER_CONNECT_ERROR);
            }
        }
        return null;
    }

    @Transactional(rollbackFor = Throwable.class)
    public GovernanceResult open(Integer brokerId, String topic, String creater, HttpServletRequest request,
            HttpServletResponse response) throws GovernanceException {
        HttpServletRequest req = (HttpServletRequest) request;

        String accountId = cookiesTools.getCookieValueByName(req, ConstantProperties.COOKIE_MGR_ACCOUNT_ID);
        BrokerEntity brokerEntity = brokerService.getBroker(brokerId);
        if (brokerEntity != null) {
            if (!accountId.equals(brokerEntity.getUserId().toString())) {
                throw new GovernanceException(ErrorCode.ACCESS_DENIED);
            }
            topicInfoMapper.openBrokeTopic(brokerId, topic, creater);

            CloseableHttpClient client = generateHttpClient(brokerEntity.getBrokerUrl());
            String url = brokerEntity.getBrokerUrl() + "/rest/open?topic=" + topic;
            log.info("url: " + url);
            HttpGet get = getMethod(url, request);

            String mes = null;
            try {
                CloseableHttpResponse closeResponse = client.execute(get);
                mes = EntityUtils.toString(closeResponse.getEntity());
            } catch (Exception e) {
                throw new GovernanceException(ErrorCode.BROKER_CONNECT_ERROR);
            }

            try {
                Boolean result = (Boolean) JSON.parse(mes);
                return new GovernanceResult(result);
            } catch (Exception e) {
                JSON json = JSON.parseObject(mes);
                @SuppressWarnings("unchecked")
                Map<String, Object> result = JSON.toJavaObject(json, Map.class);
                throw new GovernanceException((Integer) (result.get("code")), result.get("message").toString());
            }

        }
        return null;
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
