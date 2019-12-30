package com.webank.weevent.governance.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import com.webank.weevent.governance.common.ErrorCode;
import com.webank.weevent.governance.common.GovernanceException;
import com.webank.weevent.governance.common.GovernanceResult;
import com.webank.weevent.governance.entity.BrokerEntity;
import com.webank.weevent.governance.service.BrokerService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
@RequestMapping(value = "/broker")
@Slf4j
public class BrokerController {

    @Autowired
    private BrokerService brokerService;


    // get all broker service
    @GetMapping("/list")
    public List<BrokerEntity> getAllBrokers(HttpServletRequest request, @CookieValue("MGR_ACCOUNT_ID") String accountId) {
        log.info("get all brokers ");
        return brokerService.getBrokers(request, accountId);
    }

    // get broker service by id
    @GetMapping("/{id}")
    public BrokerEntity getBroker(@PathVariable("id") Integer id) {
        log.info("get  broker service by id :{}", id);
        return brokerService.getBroker(id);
    }

    // get brokerEntity service by id
    @PostMapping("/add")
    public GovernanceResult addBroker(@Valid @RequestBody BrokerEntity brokerEntity, HttpServletRequest request,
                                      HttpServletResponse response) throws GovernanceException {
        log.info("add  brokerEntity service into db brokerEntity :{} ", brokerEntity);
        return brokerService.addBroker(brokerEntity, request, response);
    }

    @PostMapping("/update")
    public GovernanceResult updateBroker(@RequestBody BrokerEntity brokerEntity, HttpServletRequest request,
                                         HttpServletResponse response) throws GovernanceException {
        log.info("update  brokerEntity service ,brokerEntity:{} ", brokerEntity);
        return brokerService.updateBroker(brokerEntity, request, response);
    }

    @PostMapping("/delete")
    public GovernanceResult deleteBroker(@RequestBody BrokerEntity brokerEntity, HttpServletRequest request) throws GovernanceException {
        log.info("delete  brokerEntity service ,id: {}", brokerEntity.getId());
        return brokerService.deleteBroker(brokerEntity, request);
    }

    @PostMapping("/checkServer")
    public ErrorCode checkServerByUrl(@RequestBody BrokerEntity brokerEntity, HttpServletRequest request) throws GovernanceException {
        log.info("checkServer  brokerEntity, id: {}", brokerEntity.getId());
        return brokerService.checkServerByUrl(brokerEntity, request);
    }
}
