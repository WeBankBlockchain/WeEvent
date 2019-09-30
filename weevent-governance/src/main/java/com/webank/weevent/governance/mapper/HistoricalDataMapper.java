package com.webank.weevent.governance.mapper;

import java.util.List;

import com.webank.weevent.governance.entity.HistoricalDataEntity;

public interface HistoricalDataMapper {

    List<HistoricalDataEntity> historicalDataList(HistoricalDataEntity historicalDataEntity);

    boolean addHistoricalData(HistoricalDataEntity historicalDataEntity);

    boolean deleteHistoricalData(HistoricalDataEntity historicalDataEntity);

    int countHistoricalData(HistoricalDataEntity historicalDataEntity);
}
