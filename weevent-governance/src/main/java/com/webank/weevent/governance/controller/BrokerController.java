package com.webank.weevent.governance.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import com.webank.weevent.governance.entity.Broker;
import com.webank.weevent.governance.service.BrokerService;

import lombok.extern.slf4j.Slf4j;

@CrossOrigin
@RestController
@Slf4j
public class BrokerController {

	@Autowired
	BrokerService brokerService;

	// get all broker service
	@GetMapping("/broker/list")
	public List<Broker> getAllBrokers() {
		log.info("get all brokers");
		return brokerService.getBrokers();
	}

	// get broker service by id
	@GetMapping("/broker/{id}")
	public Broker getBroker(@PathVariable("id") Integer id) {
		log.info("get  broker service by id = " + id);
		return brokerService.getBroker(id);
	}

	// get broker service by id
	@PostMapping("/broker")
	public Boolean addBroker(Broker broker) {
		log.info("add  broker service into db " + broker);
		return brokerService.addBroker(broker);
	}
	
	@PutMapping("/broker")
	public Boolean updateBroker(Broker broker) {
		log.info("update  broker service ,broker: " + broker);
		return brokerService.updateBroker(broker);
	}
	
	@DeleteMapping("/broker/{id}")
	public Boolean deleteBroker(@PathVariable("id") Integer id) {
		log.info("delete  broker service ,id: " + id);
		return brokerService.deleteBroker(id);
	}

}
