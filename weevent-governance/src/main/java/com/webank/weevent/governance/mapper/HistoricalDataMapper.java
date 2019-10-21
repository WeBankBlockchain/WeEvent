package com.webank.weevent.governance.mapper;

import java.util.List;

import com.webank.weevent.governance.entity.HistoricalDataEntity;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface HistoricalDataMapper {

    List<HistoricalDataEntity> historicalDataList(HistoricalDataEntity historicalDataEntity);

}
