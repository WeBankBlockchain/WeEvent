package com.webank.weevent.governance.service;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webank.weevent.governance.code.ErrorCode;
import com.webank.weevent.governance.entity.RuleDatabaseEntity;
import com.webank.weevent.governance.exception.GovernanceException;
import com.webank.weevent.governance.mapper.RuleDatabaseMapper;
import com.webank.weevent.governance.properties.ConstantProperties;
import com.webank.weevent.governance.utils.CookiesTools;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
    private RuleDatabaseMapper ruleDatabaseMapper;

    public List<RuleDatabaseEntity> circulationDatabaseList(HttpServletRequest request, RuleDatabaseEntity ruleDatabaseEntity) throws GovernanceException {
        try {
            String accountId = cookiesTools.getCookieValueByName(request, ConstantProperties.COOKIE_MGR_ACCOUNT_ID);
            if (accountId == null || !accountId.equals(ruleDatabaseEntity.getUserId().toString())) {
                throw new GovernanceException(ErrorCode.ACCESS_DENIED);
            }
            List<RuleDatabaseEntity> CirculationDatabaseEntities = null;
            ruleDatabaseEntity.setIsVisible("1");
            CirculationDatabaseEntities = ruleDatabaseMapper.circulationDatabaseList(ruleDatabaseEntity);
            return CirculationDatabaseEntities;
        } catch (Exception e) {
            log.error("get circulationDatabaseList fail", e);
            throw new GovernanceException("get circulationDatabaseList fail", e);
        }

    }


    @Transactional(rollbackFor = Throwable.class)
    public RuleDatabaseEntity addCirculationDatabase(RuleDatabaseEntity ruleDatabaseEntity, HttpServletRequest request, HttpServletResponse response)
            throws GovernanceException {
        try {
            String accountId = cookiesTools.getCookieValueByName(request, ConstantProperties.COOKIE_MGR_ACCOUNT_ID);
            if (accountId == null || !accountId.equals(ruleDatabaseEntity.getUserId().toString())) {
                throw new GovernanceException(ErrorCode.ACCESS_DENIED);
            }
            //check dbUrl
            commonService.checkDataBaseUrl(ruleDatabaseEntity.getDatabaseUrl(),ruleDatabaseEntity.getTableName());
            ruleDatabaseEntity.setIsVisible("1");
            ruleDatabaseMapper.addCirculationDatabase(ruleDatabaseEntity);
            return ruleDatabaseEntity;
        } catch (Exception e) {
            log.error("add circulationDatabaseEntity fail", e.getMessage());
            throw new GovernanceException("add circulationDatabaseEntity fail ", e);
        }
    }

    @Transactional(rollbackFor = Throwable.class)
    public boolean deleteCirculationDatabase(RuleDatabaseEntity ruleDatabaseEntity, HttpServletRequest request) throws GovernanceException {
        try {
            String accountId = cookiesTools.getCookieValueByName(request, ConstantProperties.COOKIE_MGR_ACCOUNT_ID);
            if (accountId == null || !accountId.equals(ruleDatabaseEntity.getUserId().toString())) {
                throw new GovernanceException(ErrorCode.ACCESS_DENIED);
            }
            return ruleDatabaseMapper.deleteCirculationDatabase(ruleDatabaseEntity);
        } catch (Exception e) {
            log.error("delete circulationDatabaseEntity fail", e);
            throw new GovernanceException("delete circulationDatabaseEntity fail ", e);
        }
    }

    @Transactional(rollbackFor = Throwable.class)
    public boolean updateCirculationDatabase(RuleDatabaseEntity ruleDatabaseEntity, HttpServletRequest request, HttpServletResponse response)
            throws GovernanceException {
        try {
            String accountId = cookiesTools.getCookieValueByName(request, ConstantProperties.COOKIE_MGR_ACCOUNT_ID);
            if (accountId == null || !accountId.equals(ruleDatabaseEntity.getUserId().toString())) {
                throw new GovernanceException(ErrorCode.ACCESS_DENIED);
            }
            //check databaseUrl
            commonService.checkDataBaseUrl(ruleDatabaseEntity.getDatabaseUrl(),ruleDatabaseEntity.getTableName());
            return ruleDatabaseMapper.updateCirculationDatabase(ruleDatabaseEntity);
        } catch (Exception e) {
            log.error("update circulationDatabase fail", e);
            throw new GovernanceException("update circulationDatabase fail", e);
        }

    }
}
