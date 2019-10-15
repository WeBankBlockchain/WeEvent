package com.webank.weevent.governance.mapper;

import java.util.List;

import com.webank.weevent.governance.entity.HistoricalDataEntity;
import com.webank.weevent.governance.vo.HistoricalDataVo;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface HistoricalDataMapper {

    List<HistoricalDataEntity> historicalDataList(HistoricalDataVo historicalDataVo);

}
