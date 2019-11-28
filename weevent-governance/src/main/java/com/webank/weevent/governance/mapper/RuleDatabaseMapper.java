package com.webank.weevent.governance.mapper;

import java.util.List;

import com.webank.weevent.governance.entity.RuleDatabaseEntity;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RuleDatabaseMapper {

    // get ruleDatabaseList
    List<RuleDatabaseEntity> getRuleDataBaseList(RuleDatabaseEntity ruleDatabaseEntity);

    RuleDatabaseEntity getRuleDataBaseById(Integer id);

}
