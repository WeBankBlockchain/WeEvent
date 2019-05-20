package com.webank.weevent.governance.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webank.weevent.governance.entity.Broker;
import com.webank.weevent.governance.mapper.BrokerMapper;

/**
 * 
 * BrokerService
 * @since 2019/04/28
 *
 */
@Service
public class BrokerService {
	
	@Autowired
	BrokerMapper brokerMapper;

	public List<Broker> getBrokers() {
		return brokerMapper.getBrokers();
	}

	public Broker getBroker(Integer id) {
		return brokerMapper.getBroker(id);
	}

	public Boolean addBroker(Broker broker) {
		return brokerMapper.addBroker(broker);
	}

	public Boolean deleteBroker(Integer id) {
		return brokerMapper.deleteBroker(id);
	}

	public Boolean updateBroker(Broker broker) {
		return brokerMapper.updateBroker(broker);
	}
}
