package com.webank.weevent.governance.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webank.weevent.governance.common.ErrorCode;
import com.webank.weevent.governance.common.GovernanceException;
import com.webank.weevent.governance.service.CommonService;
import com.webank.weevent.governance.utils.Utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@Slf4j
@RestController
public class ForwardController {
    private final static String brokerServiceId = "weevent-broker";

    private CommonService commonService;
    private DiscoveryClient discoveryClient;

    @Autowired
    public void setCommonService(CommonService commonService) {
        this.commonService = commonService;
    }

    @Autowired
    public void setDiscoveryClient(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    @RequestMapping(value = "/weevent-broker/admin/listGroup", method = RequestMethod.GET)
    public Object forward(HttpServletRequest request, HttpServletResponse response, @PathVariable(name = "path1") String path1, @PathVariable(name = "path2") String path2) throws GovernanceException {
        log.info("weevent url: /weevent-broker/ {}  \"/\" {}", path1, path2);

        String uri = Utils.getUrlFromDiscovery(this.discoveryClient, brokerServiceId);
        if (uri.isEmpty()) {
            log.error("unknown broker url");
            throw new GovernanceException(ErrorCode.BROKER_CONNECT_ERROR);
        }

        String forwardUrl = new StringBuffer(uri + "/" + brokerServiceId).append("/").append(path1).append("/").append(path2).toString();
        try {
            CloseableHttpResponse closeResponse = this.commonService.getCloseResponse(request, forwardUrl);
            return EntityUtils.toString(closeResponse.getEntity());
        } catch (Exception e) {
            throw new GovernanceException(e.getMessage());
        }
    }
}
