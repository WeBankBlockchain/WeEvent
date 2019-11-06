package com.webank.weevent.governance.mapper;

import java.util.List;

import com.webank.weevent.governance.entity.RuleDatabaseEntity;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RuleDatabaseMapper {

    // get circulationDatabaseList
    List<RuleDatabaseEntity> circulationDatabaseList(RuleDatabaseEntity ruleDatabaseEntity);

    RuleDatabaseEntity getRuleDataBaseById(Integer id);


    // add circulationDatabaseEntity
    Boolean addCirculationDatabase(RuleDatabaseEntity ruleDatabaseEntity);

    void batchInsert(@Param("circulationDatabaseList") List<RuleDatabaseEntity> circulationDatabaseEntities);

    // delete circulationDatabaseEntity
    Boolean deleteCirculationDatabase(RuleDatabaseEntity ruleDatabaseEntity);

    // update circulationDatabaseEntity
    Boolean updateCirculationDatabase(RuleDatabaseEntity ruleDatabaseEntity);

    //count circulationDatabaseEntity
    int countCirculationDatabase(RuleDatabaseEntity ruleDatabaseEntity);
}
