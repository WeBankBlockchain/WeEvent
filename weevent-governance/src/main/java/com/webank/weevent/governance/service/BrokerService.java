package com.webank.weevent.governance.service;

import java.util.List;
import javax.annotation.PostConstruct;

import com.webank.weevent.governance.entity.Broker;
import com.webank.weevent.governance.mapper.BrokerMapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

/**
 * 
 * BrokerService
 * 
 * @since 2019/04/28
 *
 */
@Service
@Slf4j
public class BrokerService {

    @Autowired
    BrokerMapper brokerMapper;

    @Autowired
    ApplicationContext context;

    @PostConstruct
    public void init() {
	try {
	    brokerMapper.count();
	} catch (Exception e) {
	    log.error(e.getMessage());
	    System.exit(SpringApplication.exit(context));
	    ;
	}
    }

    public List<Broker> getBrokers(Integer userId) {
	return brokerMapper.getBrokers(userId);
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
