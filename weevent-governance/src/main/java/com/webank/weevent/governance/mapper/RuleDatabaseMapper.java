package com.webank.weevent.governance.mapper;

import java.util.List;

import com.webank.weevent.governance.entity.RuleDatabaseEntity;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RuleDatabaseMapper {

    // get circulationDatabaseList
    List<RuleDatabaseEntity> getRuleDataBaseList(RuleDatabaseEntity ruleDatabaseEntity);

    RuleDatabaseEntity getRuleDataBaseById(Integer id);


    // add circulationDatabaseEntity
    Boolean addRuleDatabase(RuleDatabaseEntity ruleDatabaseEntity);

    // delete circulationDatabaseEntity
    Boolean deleteRuleDatabase(RuleDatabaseEntity ruleDatabaseEntity);

    // update circulationDatabaseEntity
    Boolean updateRuleDatabase(RuleDatabaseEntity ruleDatabaseEntity);

    //count circulationDatabaseEntity
    int countRuleDatabase(RuleDatabaseEntity ruleDatabaseEntity);
}
