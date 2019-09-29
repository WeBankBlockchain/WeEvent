package com.webank.weevent.governance.service;

import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webank.weevent.governance.code.ErrorCode;
import com.webank.weevent.governance.entity.RuleEngineEntity;
import com.webank.weevent.governance.enums.PayloadEnum;
import com.webank.weevent.governance.enums.StatusEnum;
import com.webank.weevent.governance.exception.GovernanceException;
import com.webank.weevent.governance.mapper.RuleEngineMapper;
import com.webank.weevent.governance.properties.ConstantProperties;
import com.webank.weevent.governance.utils.CookiesTools;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

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

    @Autowired
    private PermissionService permissionService;

    private final String regex = "^[a-z0-9A-Z_-]{1,100}";


    public List<RuleEngineEntity> getRuleEngines(HttpServletRequest request, RuleEngineEntity ruleEngineEntity) throws GovernanceException {
        try {
            String accountId = cookiesTools.getCookieValueByName(request, ConstantProperties.COOKIE_MGR_ACCOUNT_ID);
            if (accountId == null || !accountId.equals(ruleEngineEntity.getUserId().toString())) {
                throw new GovernanceException(ErrorCode.ACCESS_DENIED);
            }
            int count = ruleEngineMapper.countRuleEngine(ruleEngineEntity);
            List<RuleEngineEntity> ruleEngineEntities = null;
            if (count > 0) {
                ruleEngineEntity.setStartIndex((ruleEngineEntity.getPageNumber() - 1) * ruleEngineEntity.getPageSize());
                ruleEngineEntity.setEndIndex(ruleEngineEntity.getPageNumber() * ruleEngineEntity.getPageSize());
                ruleEngineEntities = ruleEngineMapper.getRuleEngines(ruleEngineEntity);
            }
            return ruleEngineEntities;
        } catch (Exception e) {
            log.error("get ruleEngines fail", e);
            throw new GovernanceException("get ruleEngines fail", e);
        }

    }


    @Transactional(rollbackFor = Throwable.class)
    public RuleEngineEntity addRuleEngine(RuleEngineEntity ruleEngineEntity, HttpServletRequest request, HttpServletResponse response)
            throws GovernanceException {
        try {
            String accountId = cookiesTools.getCookieValueByName(request, ConstantProperties.COOKIE_MGR_ACCOUNT_ID);
            if (accountId == null || !accountId.equals(ruleEngineEntity.getUserId().toString())) {
                throw new GovernanceException(ErrorCode.ACCESS_DENIED);
            }
            //Verify ruleName English letters, numbers, underscores, hyphens,length
            boolean flag = this.checkRuleName(ruleEngineEntity.getRuleName(), this.regex);
            if (!flag) {
                throw new GovernanceException("illegal ruleName format");
            }
            ruleEngineEntity.setStatus(StatusEnum.NOT_STARTED.getCode());
            String payload = JSONObject.toJSON(ruleEngineEntity.getPayloadMap()).toString();
            ruleEngineEntity.setPayload(payload);
            if (ruleEngineEntity.getPayloadType() == null || ruleEngineEntity.getPayloadType() == 0) {
                ruleEngineEntity.setPayloadType(PayloadEnum.JSON.getCode());
            }
            ruleEngineMapper.addRuleEngine(ruleEngineEntity);
            return ruleEngineEntity;
        } catch (Exception e) {
            log.error("add ruleEngineEntity fail", e);
            throw new GovernanceException("add ruleEngineEntity fail ", e);
        }
    }

    @Transactional(rollbackFor = Throwable.class)
    public boolean deleteRuleEngine(RuleEngineEntity ruleEngineEntity, HttpServletRequest request) throws GovernanceException {
        authCheck(ruleEngineEntity, request);
        // commonService.getCloseResponse()
        //delete cep ruleEngineEntity.getCepId()
        return ruleEngineMapper.deleteRuleEngine(ruleEngineEntity);
    }

    @Transactional(rollbackFor = Throwable.class)
    public boolean updateRuleEngine(RuleEngineEntity ruleEngineEntity, HttpServletRequest request, HttpServletResponse response)
            throws GovernanceException {
        try {
            authCheck(ruleEngineEntity, request);
            RuleEngineEntity rule = new RuleEngineEntity();
            rule.setId(ruleEngineEntity.getId());
            String payload = JSONObject.toJSON(ruleEngineEntity.getPayloadMap()).toString();
            ruleEngineEntity.setPayload(payload);
            List<RuleEngineEntity> ruleEngines = ruleEngineMapper.getRuleEngines(rule);
            if (CollectionUtils.isEmpty(ruleEngines)) {
                throw new GovernanceException("the data no longer exists");
            }
            //check databaseUrl
            commonService.checkDataBaseUrl(ruleEngineEntity.getDatabaseUrl());
            //updateCEPRuleById
            //判断是否有cepId，如果有就要更新cep
            Integer cepId = ruleEngines.get(0).getCepId();
            //  commonService.
            return ruleEngineMapper.updateRuleEngine(ruleEngineEntity);
        } catch (Exception e) {
            log.error("update ruleEngine fail", e);
            throw new GovernanceException("update ruleEngine fail", e);
        }

    }


    @Transactional(rollbackFor = Throwable.class)
    public boolean updateRuleEngineStatus(RuleEngineEntity ruleEngineEntity, HttpServletRequest request, HttpServletResponse response)
            throws GovernanceException {
        authCheck(ruleEngineEntity, request);
        return ruleEngineMapper.updateRuleEngineStatus(ruleEngineEntity);
    }

    @Transactional(rollbackFor = Throwable.class)
    public boolean startRuleEngine(RuleEngineEntity ruleEngineEntity, HttpServletRequest request, HttpServletResponse response)
            throws GovernanceException {
        RuleEngineEntity rule = new RuleEngineEntity();
        try {
            //query by id and status
            rule.setId(ruleEngineEntity.getId());
            rule.setStatus(StatusEnum.NOT_STARTED.getCode());
            List<RuleEngineEntity> ruleEngines = ruleEngineMapper.getRuleEngines(rule);
            if (CollectionUtils.isEmpty(ruleEngines)) {
                throw new GovernanceException("the data is not exists");
            }

            rule = ruleEngines.get(0);
      /*      String afterUrl = getAfterUrl(rule);
            String url = new StringBuffer(rule.getBrokerUrl()).append("/processor/startCEPRule?").append(afterUrl).toString();
            CloseableHttpResponse closeResponse = commonService.getCloseResponse(request, url);
            int statusCode = closeResponse.getStatusLine().getStatusCode();
            if (ErrorCode.SUCCESS.getCode() != statusCode) {
                log.error(ErrorCode.BROKER_CONNECT_ERROR.getCodeDesc());
                throw new GovernanceException(ErrorCode.BROKER_CONNECT_ERROR);
            }

            String mes = EntityUtils.toString(closeResponse.getEntity());
            JSONObject jsonObject = JSONObject.parseObject(mes);
            Integer code = Integer.valueOf(jsonObject.get("code").toString());
            if (100 != code.intValue()) {
                log.error("broker start ruleEngine fail");
                throw new GovernanceException("broker start ruleEngine fail");
            }*/

            //modify status
            RuleEngineEntity engineEntity = new RuleEngineEntity();
            engineEntity.setId(rule.getId());
            engineEntity.setStatus(StatusEnum.RUNNING.getCode());
            return ruleEngineMapper.updateRuleEngineStatus(engineEntity);
        } catch (Exception e) {
            log.error("start ruleEngine fail", e);
            throw new GovernanceException("start ruleEngine fail", e);
        }
    }

    private String getAfterUrl(RuleEngineEntity rule) {
        //Verify that the required fields are present
        return null;
    }

    public RuleEngineEntity getRuleEngineDetail(RuleEngineEntity ruleEngineEntity, HttpServletRequest request, HttpServletResponse response) {
        RuleEngineEntity rule = new RuleEngineEntity();
        rule.setId(ruleEngineEntity.getId());
        List<RuleEngineEntity> ruleEngines = ruleEngineMapper.getRuleEngines(rule);
        if (CollectionUtils.isEmpty(ruleEngines)) {
            return null;
        }
        return ruleEngines.get(0);
    }

    private boolean checkRuleName(String ruleName, String regex) {
        if (ruleName == null || ruleName.trim().length() == 0) {
            return false;
        }
        return Pattern.matches(regex, ruleName);

    }

    private void authCheck(RuleEngineEntity ruleEngineEntity, HttpServletRequest request) throws GovernanceException {
        String accountId = cookiesTools.getCookieValueByName(request, ConstantProperties.COOKIE_MGR_ACCOUNT_ID);
        Boolean flag = permissionService.verifyPermissions(ruleEngineEntity.getBrokerId(), accountId);
        if (!flag) {
            throw new GovernanceException(ErrorCode.ACCESS_DENIED);
        }
    }


}
