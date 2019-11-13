package com.webank.weevent.governance.service;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webank.weevent.governance.code.ErrorCode;
import com.webank.weevent.governance.entity.BrokerEntity;
import com.webank.weevent.governance.entity.PermissionEntity;
import com.webank.weevent.governance.entity.RuleEngineEntity;
import com.webank.weevent.governance.enums.IsCreatorEnum;
import com.webank.weevent.governance.exception.GovernanceException;
import com.webank.weevent.governance.mapper.BrokerMapper;
import com.webank.weevent.governance.mapper.PermissionMapper;
import com.webank.weevent.governance.mapper.RuleEngineMapper;
import com.webank.weevent.governance.mapper.TopicInfoMapper;
import com.webank.weevent.governance.properties.ConstantProperties;
import com.webank.weevent.governance.result.GovernanceResult;
import com.webank.weevent.governance.utils.CookiesTools;

import com.alibaba.fastjson.JSONObject;
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

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private TopicHistoricalService topicHistoricalService;

    @Autowired
    private RuleEngineService ruleEngineService;

    @Autowired
    private RuleEngineMapper ruleEngineMapper;

    @Autowired
    private CommonService commonService;

    private final static String HTTP_GET_SUCCESS_CODE = "0";

    private final static String weEventVersion = "2";

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


        //check both broker and webase serverUrl
        ErrorCode errorCode = checkServerByBrokerEntity(brokerEntity, request);
        //checkBrokerUrlRepeat
        boolean repeat = checkBrokerUrlRepeat(brokerEntity);
        if (!repeat) {
           return new GovernanceResult(ErrorCode.BROKER_REPEAT);
        }
        if (errorCode.getCode() != ErrorCode.SUCCESS.getCode()) {
            throw new GovernanceException(errorCode);
        }
        try {
            brokerMapper.addBroker(brokerEntity);
            //create permissionEntityList
            List<PermissionEntity> permissionEntityList = createPerMissionList(brokerEntity);
            if (permissionEntityList.size() > 0) {
                permissionMapper.batchInsert(permissionEntityList);
                brokerMapper.addBroker(brokerEntity);
            }
            // Create a table based on the brokerId and groupId, start a rule engine
            //determine if the processor's service is available
            boolean exist = ruleEngineService.checkProcessorExist(request);
            log.info("exist:{}", exist);
            if (exist) {
                log.info("processor exist");
                topicHistoricalService.createRule(request, response, brokerEntity);
            }
            return GovernanceResult.ok(true);
        } catch (Exception e) {
            log.error("add broker fail", e);
            throw new GovernanceException("add broker fail" + e.getMessage());
        }
    }

    private boolean checkBrokerUrlRepeat(BrokerEntity brokerEntity) {
        BrokerEntity broker = new BrokerEntity();
        broker.setBrokerUrl(brokerEntity.getBrokerUrl().trim());
        List<BrokerEntity> brokerEntities = brokerMapper.brokerList(broker);
        if (CollectionUtils.isEmpty(brokerEntities)) {
            return true;
        }
        if (brokerEntities.size() > 1) {
            return false;
        }
        return brokerEntity.getId() != null && brokerEntities.get(0).getId().intValue() == brokerEntity.getId().intValue();
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

    @Transactional(rollbackFor = Throwable.class)
    public GovernanceResult deleteBroker(BrokerEntity brokerEntity, HttpServletRequest request) throws GovernanceException {
        authCheck(brokerEntity, request);
        //delete processor rule
        RuleEngineEntity ruleEngineEntity = new RuleEngineEntity();
        ruleEngineEntity.setBrokerId(brokerEntity.getId());
        List<RuleEngineEntity> ruleEngines = ruleEngineMapper.getRuleEngines(ruleEngineEntity);
        boolean exist = ruleEngineService.checkProcessorExist(request);
        log.info("exist:{}", exist);
        try {
            if (CollectionUtils.isNotEmpty(ruleEngines) && exist) {
                for (RuleEngineEntity ruleEngine : ruleEngines) {
                    ruleEngineService.deleteProcessRule(request, ruleEngine);
                }
            }
            topicInfoMapper.deleteByBrokerId(brokerEntity.getId());
            brokerMapper.deleteBroker(brokerEntity.getId());
            permissionMapper.deletePermission(brokerEntity.getId());
        } catch (Exception e) {
            log.info("delete broker fail", e);
            throw new GovernanceException("delete broker fail" + e.getMessage());
        }

        return GovernanceResult.ok(true);
    }

    @Transactional(rollbackFor = Throwable.class)
    public GovernanceResult updateBroker(BrokerEntity brokerEntity, HttpServletRequest request, HttpServletResponse response)
            throws GovernanceException {
        authCheck(brokerEntity, request);

        //check both broker and webase serverUrl
        ErrorCode errorCode = checkServerByBrokerEntity(brokerEntity, request);
        if (errorCode.getCode() != ErrorCode.SUCCESS.getCode()) {
            throw new GovernanceException(errorCode);
        }
        //checkBrokerUrlRepeat
        boolean repeat = checkBrokerUrlRepeat(brokerEntity);
        if (!repeat) {
            return new GovernanceResult(ErrorCode.BROKER_REPEAT);
        }
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

    private ErrorCode checkServerByBrokerEntity(BrokerEntity brokerEntity, HttpServletRequest request) throws GovernanceException {
        if (StringUtils.isBlank(brokerEntity.getBrokerUrl())) {
            return ErrorCode.ILLEGAL_INPUT;
        }
        String version = this.getVersion(request, brokerEntity.getBrokerUrl());
        if (version != null && !version.startsWith(weEventVersion) && StringUtils.isBlank(brokerEntity.getBrokerUrl())) {
            return ErrorCode.WEBASE_REQUIRED;
        }
        //checkServerUrl
        return check(brokerEntity, request);
    }

    private void authCheck(BrokerEntity brokerEntity, HttpServletRequest request) throws GovernanceException {
        String accountId = cookiesTools.getCookieValueByName(request, ConstantProperties.COOKIE_MGR_ACCOUNT_ID);
        Boolean flag = permissionService.verifyPermissions(brokerEntity.getId(), accountId);
        if (!flag) {
            throw new GovernanceException(ErrorCode.ACCESS_DENIED);
        }
    }

    public ErrorCode checkServerByUrl(BrokerEntity brokerEntity, HttpServletRequest request) throws GovernanceException {
        //check broker or webase serverUrl
        if (StringUtils.isBlank(brokerEntity.getBrokerUrl()) && StringUtils.isBlank(brokerEntity.getWebaseUrl())) {
            return ErrorCode.ILLEGAL_INPUT;
        }
        String version = this.getVersion(request, brokerEntity.getBrokerUrl());
        if (version != null && !version.startsWith(weEventVersion) && StringUtils.isBlank(brokerEntity.getBrokerUrl())) {
            return ErrorCode.WEBASE_REQUIRED;
        }
        return check(brokerEntity, request);
    }

    private ErrorCode check(BrokerEntity brokerEntity, HttpServletRequest request) {
        String accountId = cookiesTools.getCookieValueByName(request, ConstantProperties.COOKIE_MGR_ACCOUNT_ID);
        if (accountId == null || !accountId.equals(brokerEntity.getUserId().toString())) {
            return ErrorCode.ACCESS_DENIED;
        }

        if (!StringUtils.isBlank(brokerEntity.getBrokerUrl())) {
            String brokerServerUrl = brokerEntity.getBrokerUrl();
            log.info("check Broker server, url:{}", brokerServerUrl);
            try {
                checkUrl(brokerServerUrl, ConstantProperties.BROKER_LIST_URL, request);
            } catch (GovernanceException e) {
                log.error("check Broker server failed. e", e);
                return ErrorCode.BROKER_CONNECT_ERROR;
            }
        }
        if (!StringUtils.isBlank(brokerEntity.getWebaseUrl())) {
            String WebaseServerUrl = brokerEntity.getWebaseUrl();
            log.info("check WeBase server, url:{}", WebaseServerUrl);
            try {
                checkUrl(WebaseServerUrl, ConstantProperties.WEBASE_NODE_URL, request);
            } catch (GovernanceException e) {
                log.error("check WeBase server failed. e", e);
                return ErrorCode.WEBASE_CONNECT_ERROR;
            }
        }

        return ErrorCode.SUCCESS;
    }

    private void checkUrl(String url, String afterUrl, HttpServletRequest request) throws GovernanceException {
        // get httpclient
        String headUrl = url;
        CloseableHttpClient client = commonService.generateHttpClient(headUrl);
        // get one of broker urls
        headUrl = headUrl + afterUrl;
        HttpGet get = commonService.getMethod(headUrl, request);
        JSONObject jsonObject;

        try {
            CloseableHttpResponse response = client.execute(get);
            String responseResult = EntityUtils.toString(response.getEntity());
            jsonObject = JSONObject.parseObject(responseResult);
        } catch (Exception e) {
            log.error("url {}, connect fail,error:{}", headUrl, e.getMessage());
            throw new GovernanceException("url:{}" + headUrl + " connect fail", e);
        }

        if (!HTTP_GET_SUCCESS_CODE.equals(String.valueOf(jsonObject.get("code")))) {
            log.error("url {}, connect fail.", headUrl);
            throw new GovernanceException("url " + headUrl + " connect fail");
        }
    }

    public String getVersion(HttpServletRequest request, String brokerUrl) throws GovernanceException {
        String versionUrl = brokerUrl + ConstantProperties.BROKER_LIST_URL;
        try {
            CloseableHttpResponse versionResponse = commonService.getCloseResponse(request, versionUrl);
            String mes = EntityUtils.toString(versionResponse.getEntity());
            JSONObject jsonObject = JSONObject.parseObject(mes);
            return jsonObject.get("weEventVersion") == null ? null : jsonObject.get("weEventVersion").toString();
        } catch (Exception e) {
            log.error("get version fail,error:", e);
            throw new GovernanceException("get version fail,error:{}");
        }
    }
}
