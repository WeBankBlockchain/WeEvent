package com.webank.weevent.governance.mapper;

import java.util.List;

import com.webank.weevent.governance.entity.CirculationDatabaseEntity;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CirculationDatabaseMapper {

    // get circulationDatabaseList
    List<CirculationDatabaseEntity> circulationDatabaseList(CirculationDatabaseEntity circulationDatabaseEntity);

    // add circulationDatabaseEntity
    Boolean addCirculationDatabase(CirculationDatabaseEntity circulationDatabaseEntity);

    void batchInsert(@Param("circulationDatabaseList") List<CirculationDatabaseEntity> circulationDatabaseEntities);

    // delete circulationDatabaseEntity
    Boolean deleteCirculationDatabase(CirculationDatabaseEntity circulationDatabaseEntity);

    // update circulationDatabaseEntity
    Boolean updateCirculationDatabase(CirculationDatabaseEntity circulationDatabaseEntity);

    //count circulationDatabaseEntity
    int countCirculationDatabase(CirculationDatabaseEntity circulationDatabaseEntity);
}
