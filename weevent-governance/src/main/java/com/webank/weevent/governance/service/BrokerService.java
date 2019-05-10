package com.webank.weevent.governance.service;

import java.util.List;

import com.webank.weevent.governance.entity.Broker;

public interface BrokerService {

	// get Brokers
	List<Broker> getBrokers();

	// get Broker by id
	Broker getBroker(Integer id);

	// add Broker
	Boolean addBroker(Broker broker);

	// delete Broker
	Boolean deleteBroker(Integer id);
	
	//update Broker
	Boolean updateBroker(Broker broker);
}
