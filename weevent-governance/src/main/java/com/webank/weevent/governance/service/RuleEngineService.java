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
public class RuleEngineService {

    @Autowired
    private RuleEngineMapper ruleEngineMapper;

    @Autowired
    private CommonService commonService;


    @Autowired
    private CookiesTools cookiesTools;

    public List<RuleEngineEntity> getRuleEngines(HttpServletRequest request, RuleEngineEntity ruleEngineEntity) throws GovernanceException {
        String accountId = cookiesTools.getCookieValueByName(request, ConstantProperties.COOKIE_MGR_ACCOUNT_ID);
        List<RuleEngineEntity> ruleEngineEntities = ruleEngineMapper.getRuleEngines(ruleEngineEntity);
        return ruleEngineEntities;
    }


    @Transactional(rollbackFor = Throwable.class)
    public boolean addRuleEngine(RuleEngineEntity ruleEngineEntity, HttpServletRequest request, HttpServletResponse response)
            throws GovernanceException {

        return ruleEngineMapper.addRuleEngine(ruleEngineEntity);
    }

    @Transactional(rollbackFor = Throwable.class)
    public boolean deleteRuleEngine(RuleEngineEntity ruleEngineEntity, HttpServletRequest request) throws GovernanceException {
        //authCheck(ruleEngineEntity, request);
        return ruleEngineMapper.deleteRuleEngine(ruleEngineEntity);
    }

    @Transactional(rollbackFor = Throwable.class)
    public boolean updateRuleEngine(RuleEngineEntity ruleEngineEntity, HttpServletRequest request, HttpServletResponse response)
            throws GovernanceException {
        //  authCheck(ruleEngineEntity, request);
        return ruleEngineMapper.updateRuleEngine(ruleEngineEntity);
    }
}
