package com.webank.governance.junit;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.webank.governance.JUnitTestBase;
import com.webank.weevent.governance.entity.Broker;
import com.webank.weevent.governance.service.BrokerService;

public class BrokerServiceTest extends JUnitTestBase{
	
	@Autowired
	private BrokerService brokerService;
	
	@Test
	public void testAddBroker() {
		Broker broker = new Broker();
		broker.setName("brokerTestName");
		broker.setBrokerUrl("http://127.0.0.1:8080");
		Boolean result = brokerService.addBroker(broker );
		Assert.assertTrue(result);
	}
	
	@Test
	public void testGetBrokers() {
		List<Broker> result = brokerService.getBrokers();
		Assert.assertTrue(result.size() > 0);
	}
	
	@Test
	public void testGetBroker() {
		Broker result = brokerService.getBroker(1);
		Assert.assertTrue(result.getId() == 1);
	}
	
	@Test
	public void testUpdateBroker() {
		Broker broker = brokerService.getBroker(3);
		broker.setBrokerUrl("http://127.0.0.1:8088/weevent");
		Boolean result = brokerService.updateBroker(broker);
		Assert.assertTrue(result);
	}
	
	@Test
	public void testDeleteBroker() {
		Boolean result = brokerService.deleteBroker(3);
		Assert.assertTrue(result);
	}

}
