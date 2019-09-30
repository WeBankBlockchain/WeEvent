package com.webank.weevent.governance.controller;


import com.webank.weevent.governance.exception.GovernanceException;
import com.webank.weevent.governance.result.GovernanceResult;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
@RequestMapping(value = "/historicalData")
public class HistoricalDataController {

    public GovernanceResult HistoricalDataList()throws GovernanceException {
        GovernanceResult governanceResult = new GovernanceResult();

        return  governanceResult;
    }

}
