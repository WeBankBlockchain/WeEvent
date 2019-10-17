package com.webank.weevent.governance.service;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webank.weevent.governance.code.ErrorCode;
import com.webank.weevent.governance.entity.CirculationDatabaseEntity;
import com.webank.weevent.governance.exception.GovernanceException;
import com.webank.weevent.governance.mapper.CirculationDatabaseMapper;
import com.webank.weevent.governance.properties.ConstantProperties;
import com.webank.weevent.governance.utils.CookiesTools;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class CirculationDatabaseService {

    @Autowired
    private CommonService commonService;

    @Autowired
    private CookiesTools cookiesTools;

    @Autowired
    private CirculationDatabaseMapper circulationDatabaseMapper;

    public List<CirculationDatabaseEntity> circulationDatabaseList(HttpServletRequest request, CirculationDatabaseEntity CirculationDatabaseEntity) throws GovernanceException {
        try {
            String accountId = cookiesTools.getCookieValueByName(request, ConstantProperties.COOKIE_MGR_ACCOUNT_ID);
            if (accountId == null || !accountId.equals(CirculationDatabaseEntity.getUserId().toString())) {
                throw new GovernanceException(ErrorCode.ACCESS_DENIED);
            }
            List<CirculationDatabaseEntity> CirculationDatabaseEntities = null;
            CirculationDatabaseEntities = circulationDatabaseMapper.circulationDatabaseList(CirculationDatabaseEntity);
            return CirculationDatabaseEntities;
        } catch (Exception e) {
            log.error("get circulationDatabaseList fail", e);
            throw new GovernanceException("get circulationDatabaseList fail", e);
        }

    }


    @Transactional(rollbackFor = Throwable.class)
    public CirculationDatabaseEntity addCirculationDatabase(CirculationDatabaseEntity circulationDatabaseEntity, HttpServletRequest request, HttpServletResponse response)
            throws GovernanceException {
        try {
            String accountId = cookiesTools.getCookieValueByName(request, ConstantProperties.COOKIE_MGR_ACCOUNT_ID);
            if (accountId == null || !accountId.equals(circulationDatabaseEntity.getUserId().toString())) {
                throw new GovernanceException(ErrorCode.ACCESS_DENIED);
            }
            //check dbUrl
            commonService.checkDataBaseUrl(circulationDatabaseEntity.getDatabaseUrl());
            circulationDatabaseMapper.addCirculationDatabase(circulationDatabaseEntity);
            return circulationDatabaseEntity;
        } catch (Exception e) {
            log.error("add circulationDatabaseEntity fail", e.getMessage());
            throw new GovernanceException("add circulationDatabaseEntity fail ", e);
        }
    }

    @Transactional(rollbackFor = Throwable.class)
    public boolean deleteCirculationDatabase(CirculationDatabaseEntity circulationDatabaseEntity, HttpServletRequest request) throws GovernanceException {
        try {
            String accountId = cookiesTools.getCookieValueByName(request, ConstantProperties.COOKIE_MGR_ACCOUNT_ID);
            if (accountId == null || !accountId.equals(circulationDatabaseEntity.getUserId().toString())) {
                throw new GovernanceException(ErrorCode.ACCESS_DENIED);
            }
            return circulationDatabaseMapper.deleteCirculationDatabase(circulationDatabaseEntity);
        } catch (Exception e) {
            log.error("delete circulationDatabaseEntity fail", e);
            throw new GovernanceException("delete circulationDatabaseEntity fail ", e);
        }
    }

    @Transactional(rollbackFor = Throwable.class)
    public boolean updateCirculationDatabase(CirculationDatabaseEntity circulationDatabaseEntity, HttpServletRequest request, HttpServletResponse response)
            throws GovernanceException {
        try {
            String accountId = cookiesTools.getCookieValueByName(request, ConstantProperties.COOKIE_MGR_ACCOUNT_ID);
            if (accountId == null || !accountId.equals(circulationDatabaseEntity.getUserId().toString())) {
                throw new GovernanceException(ErrorCode.ACCESS_DENIED);
            }
            //check databaseUrl
            commonService.checkDataBaseUrl(circulationDatabaseEntity.getDatabaseUrl());
            return circulationDatabaseMapper.updateCirculationDatabase(circulationDatabaseEntity);
        } catch (Exception e) {
            log.error("update circulationDatabase fail", e);
            throw new GovernanceException("update circulationDatabase fail", e);
        }

    }
}
