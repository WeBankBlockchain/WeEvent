package com.webank.weevent.governance.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webank.weevent.governance.entity.Broker;
import com.webank.weevent.governance.mapper.BrokerMapper;
import com.webank.weevent.governance.service.BrokerService;


/**
 * 
 * BrokerServiceImpl
 * @since 2019/04/28
 *
 */
@Service
public class BrokerServiceImpl implements BrokerService {
	
	@Autowired
	BrokerMapper brokerMapper;

	@Override
	public List<Broker> getBrokers() {
		return brokerMapper.getBrokers();
	}

	@Override
	public Broker getBroker(Integer id) {
		return brokerMapper.getBroker(id);
	}

	@Override
	public Boolean addBroker(Broker broker) {
		return brokerMapper.addBroker(broker);
	}

	@Override
	public Boolean deleteBroker(Integer id) {
		return brokerMapper.deleteBroker(id);
	}

	@Override
	public Boolean updateBroker(Broker broker) {
		return brokerMapper.updateBroker(broker);
	}

}
