package com.webank.weevent.governance.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webank.weevent.governance.common.ConstantProperties;
import com.webank.weevent.governance.common.GovernanceException;
import com.webank.weevent.governance.service.CommonService;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@Slf4j
@RestController
public class ForwardController {

    private CommonService commonService;

    @Autowired
    public void setCommonService(CommonService commonService) {
        this.commonService = commonService;
    }


    @RequestMapping(value = "/admin/testListGroup", method = RequestMethod.GET)
    public Object forward(HttpServletRequest request, HttpServletResponse response, @RequestParam("brokerUrl") String brokerUrl) throws GovernanceException {
        String forwardUrl = brokerUrl + ConstantProperties.REST_LIST_SUBSCRIPTION;
        try {
            CloseableHttpResponse closeResponse = this.commonService.getCloseResponse(request, forwardUrl);
            return EntityUtils.toString(closeResponse.getEntity());
        } catch (Exception e) {
            throw new GovernanceException(e.getMessage());
        }
    }
}
