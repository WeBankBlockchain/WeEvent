package com.webank.weevent.governance.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webank.weevent.governance.exception.GovernanceException;
import com.webank.weevent.governance.service.CommonService;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@Slf4j
@RestController
public class ForwardController {


    @Autowired
    private CommonService commonService;

    @Value("${weevent.url}")
    private String url;

    @RequestMapping(value = "/weevent/{path1}/{path2}", method = RequestMethod.GET)
    public Object forward(HttpServletRequest request, HttpServletResponse response, @PathVariable(name = "path1") String path1, @PathVariable(name = "path2") String path2)throws GovernanceException {
        log.info("weevent url: /wevent/ {}  \"/\" {}", path1, path2);
        String forwarUrl = new StringBuffer(this.url).append("/").append(path1).append("/").append(path2).toString();
        try {
            CloseableHttpResponse closeResponse = commonService.getCloseResponse(request, forwarUrl);
            return EntityUtils.toString(closeResponse.getEntity());
        } catch (Exception e) {
            throw new GovernanceException(e.getMessage());
        }
    }


}
