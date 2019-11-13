package com.webank.weevent.governance.mapper;

import java.util.List;

import com.webank.weevent.governance.entity.BrokerEntity;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BrokerMapper {

    // get BrokerEntity by id
    BrokerEntity getBroker(Integer id);

    List<BrokerEntity> brokerList(BrokerEntity brokerEntity);

    // get Brokers
    List<BrokerEntity> getBrokers(Integer userId);

    // add BrokerEntity
    Boolean addBroker(BrokerEntity brokerEntity);

    // delete BrokerEntity
    Boolean deleteBroker(Integer id);

    // update BrokerEntity
    Boolean updateBroker(BrokerEntity brokerEntity);

    int countBroker();

}
