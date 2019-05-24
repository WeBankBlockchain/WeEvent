package com.webank.weevent.governance.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.webank.weevent.governance.entity.Account;
import com.webank.weevent.governance.result.GovernanceResult;
import com.webank.weevent.governance.service.RegisterService;

@RestController
public class RegitsterController {
	
	@Autowired
	private RegisterService registerService;

	@RequestMapping("/user/check/{param}/{type}")
	public GovernanceResult checkData(@PathVariable String param, @PathVariable Integer type) {
		GovernanceResult governanceResult = registerService.checkData(param, type);
		return governanceResult;
	}
	
	@RequestMapping(value="/user/register", method=RequestMethod.POST)
	public GovernanceResult register(@RequestBody Account user) {
		GovernanceResult governanceResult = registerService.register(user);
		return governanceResult;
	}
}
