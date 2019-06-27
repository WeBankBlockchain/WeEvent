package com.webank.weevent.governance.controller;

import java.util.List;
import javax.validation.Valid;

import com.webank.weevent.governance.entity.Broker;
import com.webank.weevent.governance.exception.GovernanceException;
import com.webank.weevent.governance.result.GovernanceResult;
import com.webank.weevent.governance.service.AccountService;
import com.webank.weevent.governance.service.BrokerService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
@RequestMapping(value = "/broker")
@Slf4j
public class BrokerController {

    @Autowired
    BrokerService brokerService;

    @Autowired
    AccountService accountService;

    // get all broker service
    @GetMapping("/list")
    public List<Broker> getAllBrokers(@RequestParam Integer userId) {
        log.info("get all brokers by userId = " + userId);

        return brokerService.getBrokers(userId);
    }

    // get broker service by id
    @GetMapping("/{id}")
    public Broker getBroker(@PathVariable("id") Integer id) {
        log.info("get  broker service by id = " + id);
        return brokerService.getBroker(id);
    }

    // get broker service by id
    @PostMapping("/add")
    public GovernanceResult addBroker(@Valid @RequestBody Broker broker) throws GovernanceException {
        log.info("add  broker service into db " + broker);
        return brokerService.addBroker(broker);
    }

    @PostMapping("/update")
    public GovernanceResult updateBroker(@RequestBody Broker broker) throws GovernanceException {
        log.info("update  broker service ,broker: " + broker);
        return brokerService.updateBroker(broker);
    }

    @GetMapping("/delete/{id}")
    public Boolean deleteBroker(@PathVariable("id") Integer id) {
        log.info("delete  broker service ,id: " + id);
        return brokerService.deleteBroker(id);
    }
}
