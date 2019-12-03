package com.webank.weevent.governance.mapper;

import java.util.List;

import com.webank.weevent.governance.entity.BrokerEntity;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface BrokerMapper {

    // get Brokers
    List<BrokerEntity> getBrokers(Integer userId);

}
