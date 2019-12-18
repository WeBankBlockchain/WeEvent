package com.webank.weevent.governance.service;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webank.weevent.governance.code.ErrorCode;
import com.webank.weevent.governance.entity.RuleDatabaseEntity;
import com.webank.weevent.governance.exception.GovernanceException;
import com.webank.weevent.governance.properties.ConstantProperties;
import com.webank.weevent.governance.repository.RuleDatabaseRepository;
import com.webank.weevent.governance.utils.CookiesTools;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.helper.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class RuleDatabaseService {

    @Autowired
    private CommonService commonService;

    @Autowired
    private CookiesTools cookiesTools;

    @Autowired
    private RuleDatabaseRepository ruleDatabaseRepository;

    public List<RuleDatabaseEntity> getRuleDataBaseList(HttpServletRequest request, RuleDatabaseEntity ruleDatabaseEntity) throws GovernanceException {
        try {
            String accountId = cookiesTools.getCookieValueByName(request, ConstantProperties.COOKIE_MGR_ACCOUNT_ID);
            if (accountId == null || !accountId.equals(ruleDatabaseEntity.getUserId().toString())) {
                throw new GovernanceException(ErrorCode.ACCESS_DENIED);
            }
            ruleDatabaseEntity.setSystemTag(false);
            Example<RuleDatabaseEntity> entityExample = Example.of(ruleDatabaseEntity);
            List<RuleDatabaseEntity> ruleDatabaseEntityList = ruleDatabaseRepository.findAll(entityExample);
            ruleDatabaseEntityList.forEach(ruleDataBase -> {
                String dataBaseUrl = ruleDataBase.getDatabaseUrl();
                if (StringUtil.isBlank(ruleDataBase.getOptionalParameter())) {
                    ruleDataBase.setDatabaseUrl(dataBaseUrl);
                } else {
                    ruleDataBase.setDatabaseUrl(dataBaseUrl + "?" + ruleDataBase.getOptionalParameter());
                }
            });
            return ruleDatabaseEntityList;
        } catch (Exception e) {
            log.error("get ruleDatabaseList fail", e);
            throw new GovernanceException("get ruleDatabaseList fail", e);
        }

    }


    @Transactional(rollbackFor = Throwable.class)
    public RuleDatabaseEntity addRuleDatabase(RuleDatabaseEntity ruleDatabaseEntity, HttpServletRequest request, HttpServletResponse response)
            throws GovernanceException {
        try {
            String accountId = cookiesTools.getCookieValueByName(request, ConstantProperties.COOKIE_MGR_ACCOUNT_ID);
            if (accountId == null || !accountId.equals(ruleDatabaseEntity.getUserId().toString())) {
                throw new GovernanceException(ErrorCode.ACCESS_DENIED);
            }
            //check dbUrl
            commonService.checkDataBaseUrl(ruleDatabaseEntity.getDatabaseUrl(), ruleDatabaseEntity.getTableName(), ruleDatabaseEntity.getUsername(), ruleDatabaseEntity.getPassword());
            ruleDatabaseEntity.setSystemTag(false);
            ruleDatabaseRepository.save(ruleDatabaseEntity);
            return ruleDatabaseEntity;
        } catch (Exception e) {
            log.error("add ruleDatabaseEntity fail", e);
            throw new GovernanceException("add ruleDatabaseEntity fail ", e);
        }
    }

    @Transactional(rollbackFor = Throwable.class)
    public void deleteRuleDatabase(RuleDatabaseEntity ruleDatabaseEntity, HttpServletRequest request) throws GovernanceException {
        try {
            String accountId = cookiesTools.getCookieValueByName(request, ConstantProperties.COOKIE_MGR_ACCOUNT_ID);
            if (accountId == null || !accountId.equals(ruleDatabaseEntity.getUserId().toString())) {
                throw new GovernanceException(ErrorCode.ACCESS_DENIED);
            }
            RuleDatabaseEntity databaseEntity = ruleDatabaseRepository.findById(ruleDatabaseEntity.getId());
            if (databaseEntity == null) {
                return;
            }
            ruleDatabaseRepository.delete(databaseEntity);
        } catch (Exception e) {
            log.error("delete ruleDatabaseEntity fail", e);
            throw new GovernanceException("delete ruleDatabaseEntity fail ", e);
        }
    }

    @Transactional(rollbackFor = Throwable.class)
    public void updateRuleDatabase(RuleDatabaseEntity ruleDatabaseEntity, HttpServletRequest request, HttpServletResponse response)
            throws GovernanceException {
        try {
            String accountId = cookiesTools.getCookieValueByName(request, ConstantProperties.COOKIE_MGR_ACCOUNT_ID);
            if (accountId == null || !accountId.equals(ruleDatabaseEntity.getUserId().toString())) {
                throw new GovernanceException(ErrorCode.ACCESS_DENIED);
            }
            ruleDatabaseEntity.setSystemTag(false);
            //check databaseUrl
            commonService.checkDataBaseUrl(ruleDatabaseEntity.getDatabaseUrl(), ruleDatabaseEntity.getTableName(), ruleDatabaseEntity.getUsername(), ruleDatabaseEntity.getPassword());
            ruleDatabaseEntity.setLastUpdate(new Date());
            ruleDatabaseRepository.save(ruleDatabaseEntity);
        } catch (Exception e) {
            log.error("update ruleDatabase fail", e);
            throw new GovernanceException("update ruleDatabase fail", e);
        }

    }

    public void checkRuleDataBaseUrl(RuleDatabaseEntity ruleDatabaseEntity, HttpServletRequest request) throws GovernanceException {
        try {
            commonService.checkDataBaseUrl(ruleDatabaseEntity.getDatabaseUrl(), ruleDatabaseEntity.getTableName(), ruleDatabaseEntity.getUsername(),
                    ruleDatabaseEntity.getPassword());
        } catch (Exception e) {
            log.error("database url is incorrect", e);
            throw new GovernanceException("database url is incorrect", e);
        }
    }
}
