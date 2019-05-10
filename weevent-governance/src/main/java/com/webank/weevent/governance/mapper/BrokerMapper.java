package com.webank.weevent.governance.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.webank.weevent.governance.entity.Broker;

@Mapper
public interface BrokerMapper {
	
	//get Broker by id
	Broker getBroker(Integer id);
	
	//get Brokers
	List<Broker> getBrokers();
	
	//add Broker
	Boolean addBroker(Broker broker);
	
	//delete Broker
	Boolean deleteBroker(Integer id);
	
	//update Broker
	Boolean updateBroker(Broker broker);

}
