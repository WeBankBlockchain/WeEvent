package com.webank.weevent.governance.service;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webank.weevent.governance.common.GovernanceException;
import com.webank.weevent.governance.entity.RuleDatabaseEntity;
import com.webank.weevent.governance.enums.CheckTypeEnum;
import com.webank.weevent.governance.enums.DatabaseTypeEnum;
import com.webank.weevent.governance.enums.IsDeleteEnum;
import com.webank.weevent.governance.repository.RuleDatabaseRepository;
import com.webank.weevent.governance.repository.RuleEngineRepository;

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
    private RuleDatabaseRepository ruleDatabaseRepository;

    @Autowired
    private RuleEngineRepository ruleEngineRepository;

    public List<RuleDatabaseEntity> getRuleDataBaseList(HttpServletRequest request, RuleDatabaseEntity ruleDatabaseEntity) throws GovernanceException {
        try {
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

        //check name repeat
        int count = ruleDatabaseRepository.countAllByDatasourceName(ruleDatabaseEntity.getDatasourceName());
        if (count > 0) {
            throw new GovernanceException("The data source name already exists");
        }
        try {
            //check dbUrl
            getDataBaseUrl(ruleDatabaseEntity);
            commonService.checkDataBaseUrl(ruleDatabaseEntity.getDatabaseType(), ruleDatabaseEntity.getDatabaseUrl(), null, ruleDatabaseEntity.getUsername(), ruleDatabaseEntity.getPassword());
            ruleDatabaseEntity.setSystemTag(false);
            ruleDatabaseRepository.save(ruleDatabaseEntity);
            return ruleDatabaseEntity;
        } catch (Exception e) {
            log.error("add ruleDatabaseEntity fail", e);
            throw new GovernanceException("add ruleDatabaseEntity fail ", e);
        }
    }

    private void getDataBaseUrl(RuleDatabaseEntity ruleDatabaseEntity) {
        String dataBaseUrl = "";
        if (ruleDatabaseEntity.getDatabaseUrl() != null) {
            return;
        }
        // 1 h2, 2 mysql
        if (DatabaseTypeEnum.H2_DATABASE.getCode().equals(ruleDatabaseEntity.getDatabaseType())) {
            dataBaseUrl = "jdbc:h2:tcp://" + ruleDatabaseEntity.getDatabaseIp() + ":" + ruleDatabaseEntity.getDatabasePort()
                    + "/" + ruleDatabaseEntity.getDatabaseName();
        } else {
            dataBaseUrl = "jdbc:mysql://" + ruleDatabaseEntity.getDatabaseIp() + ":" + ruleDatabaseEntity.getDatabasePort()
                    + "/" + ruleDatabaseEntity.getDatabaseName();
        }
        ruleDatabaseEntity.setDatabaseUrl(dataBaseUrl);
    }

    @Transactional(rollbackFor = Throwable.class)
    public void deleteRuleDatabase(RuleDatabaseEntity ruleDatabaseEntity, HttpServletRequest request) throws GovernanceException {
        try {
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
        //check used
        int count = ruleEngineRepository.countAllByRuleDataBaseIdAndDeleteAt(ruleDatabaseEntity.getId(), IsDeleteEnum.NOT_DELETED.getCode());
        if (count > 0) {
            throw new GovernanceException("This data source is being used by the rules engine and cannot be modified");
        }
        try {
            ruleDatabaseEntity.setSystemTag(false);
            //check databaseUrl
            getDataBaseUrl(ruleDatabaseEntity);
            commonService.checkDataBaseUrl(ruleDatabaseEntity.getDatabaseType(), ruleDatabaseEntity.getDatabaseUrl(), null, ruleDatabaseEntity.getUsername(), ruleDatabaseEntity.getPassword());
            ruleDatabaseEntity.setLastUpdate(new Date());
            ruleDatabaseRepository.save(ruleDatabaseEntity);
        } catch (Exception e) {
            log.error("update ruleDatabase fail", e);
            throw new GovernanceException("update ruleDatabase fail", e);
        }

    }

    public void checkRuleDataBaseUrl(RuleDatabaseEntity ruleDatabaseEntity, HttpServletRequest request) throws GovernanceException {
        try {
            // 1 check database, 2 check tableName
            if (CheckTypeEnum.CHECK_DATABASE.getCode().equals(ruleDatabaseEntity.getCheckType())) {
                getDataBaseUrl(ruleDatabaseEntity);
                commonService.checkDataBaseUrl(ruleDatabaseEntity.getDatabaseType(), ruleDatabaseEntity.getDatabaseUrl(), null, ruleDatabaseEntity.getUsername(),
                        ruleDatabaseEntity.getPassword());
            } else {
                RuleDatabaseEntity ruleDatabase = ruleDatabaseRepository.findById(ruleDatabaseEntity.getId());
                if (ruleDatabase == null) {
                    throw new GovernanceException("database record is not exists");
                }
                commonService.checkDataBaseUrl(ruleDatabase.getDatabaseType(), ruleDatabase.getDatabaseUrl(), ruleDatabase.getTableName(), ruleDatabase.getUsername(),
                        ruleDatabase.getPassword());
            }

        } catch (Exception e) {
            log.error("check failed", e);
            throw new GovernanceException("check failed", e);
        }
    }
}
