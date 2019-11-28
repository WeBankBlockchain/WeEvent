package com.webank.weevent.governance.mapper;

import java.util.List;

import com.webank.weevent.governance.entity.BrokerEntity;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface BrokerMapper {

    // get BrokerEntity by id
    BrokerEntity getBroker(Integer id);

    List<BrokerEntity> brokerList(BrokerEntity brokerEntity);

    // get Brokers
    List<BrokerEntity> getBrokers(Integer userId);


    // delete BrokerEntity
    Boolean deleteBroker(@Param("id") Integer id, @Param("deleteAt") String deleteAt);

}
