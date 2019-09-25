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

    private final String REGEX = "^[a-z0-9A-Z_-]{1,100}";


    public List<RuleEngineEntity> getRuleEngines(HttpServletRequest request, RuleEngineEntity ruleEngineEntity) throws GovernanceException {
        try {
            String accountId = cookiesTools.getCookieValueByName(request, ConstantProperties.COOKIE_MGR_ACCOUNT_ID);
            if (accountId == null || !accountId.equals(ruleEngineEntity.getUserId().toString())) {
                throw new GovernanceException(ErrorCode.ACCESS_DENIED);
            }
            int count = ruleEngineMapper.countRuleEngine(ruleEngineEntity);
            List<RuleEngineEntity> ruleEngineEntities = null;
            if (count > 0) {
                ruleEngineEntities = ruleEngineMapper.getRuleEngines(ruleEngineEntity);
            }
            return ruleEngineEntities;
        } catch (Exception e) {
            log.error("get ruleEngines fail", e);
            throw new GovernanceException("get ruleEngines fail", e);
        }

    }


    @Transactional(rollbackFor = Throwable.class)
    public boolean addRuleEngine(RuleEngineEntity ruleEngineEntity, HttpServletRequest request, HttpServletResponse response)
            throws GovernanceException {
        String accountId = cookiesTools.getCookieValueByName(request, ConstantProperties.COOKIE_MGR_ACCOUNT_ID);
        if (accountId == null || !accountId.equals(ruleEngineEntity.getUserId().toString())) {
            throw new GovernanceException(ErrorCode.ACCESS_DENIED);
        }
        try {
            //Verify ruleName English letters, numbers, underscores, hyphens,length
            boolean flag = this.checkRuleName(ruleEngineEntity.getRuleName(), this.REGEX);
            if (!flag) {
                throw new GovernanceException("illegal ruleName format");
            }
            ruleEngineEntity.setStatus(StatusEnum.NOT_STARTED.getCode());
            String payload = JSONObject.toJSON(ruleEngineEntity.getPayloadMap()).toString();
            ruleEngineEntity.setPayload(payload);
            if (ruleEngineEntity.getPayloadType() == null || ruleEngineEntity.getPayloadType() == 0) {
                ruleEngineEntity.setPayloadType(PayloadEnum.JSON.getCode());
            }
            return ruleEngineMapper.addRuleEngine(ruleEngineEntity);
        } catch (Exception e) {
            log.error("add ruleEngineEntity fail", e);
            throw new GovernanceException("add ruleEngineEntity fail ", e);
        }
    }

    @Transactional(rollbackFor = Throwable.class)
    public boolean deleteRuleEngine(RuleEngineEntity ruleEngineEntity, HttpServletRequest request) throws GovernanceException {
        authCheck(ruleEngineEntity, request);
        return ruleEngineMapper.deleteRuleEngine(ruleEngineEntity);
    }

    @Transactional(rollbackFor = Throwable.class)
    public boolean updateRuleEngine(RuleEngineEntity ruleEngineEntity, HttpServletRequest request, HttpServletResponse response)
            throws GovernanceException {
        authCheck(ruleEngineEntity, request);
        RuleEngineEntity rule = new RuleEngineEntity();
        rule.setId(ruleEngineEntity.getId());
        String payload = JSONObject.toJSON(ruleEngineEntity.getPayloadMap()).toString();
        ruleEngineEntity.setPayload(payload);
        List<RuleEngineEntity> ruleEngines = ruleEngineMapper.getRuleEngines(rule);
        if (CollectionUtils.isEmpty(ruleEngines)) {
            throw new GovernanceException("the data no longer exists");
        }
        return ruleEngineMapper.updateRuleEngine(ruleEngineEntity);
    }

    @Transactional(rollbackFor = Throwable.class)
    public boolean updateRuleEngineStatus(RuleEngineEntity ruleEngineEntity, HttpServletRequest request, HttpServletResponse response)
            throws GovernanceException {
        authCheck(ruleEngineEntity, request);
        return ruleEngineMapper.updateRuleEngineStatus(ruleEngineEntity);
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
