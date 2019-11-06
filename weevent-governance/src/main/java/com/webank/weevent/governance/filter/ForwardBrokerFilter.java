package com.webank.weevent.governance.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webank.weevent.governance.entity.BrokerEntity;
import com.webank.weevent.governance.service.BrokerService;
import com.webank.weevent.governance.service.CommonService;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.jsoup.helper.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ForwardBrokerFilter implements Filter {

    @Autowired
    private BrokerService brokerService;

    @Autowired
    private CommonService commonService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        String idStr = request.getParameter("brokerId");
        String brokerUrl = request.getParameter("brokerUrl");
        String originUrl = req.getRequestURI();
        // get tail of brokerEntity url
        String subStrUrl = originUrl.substring(originUrl.indexOf("/weevent/") + "/weevent".length());
        if (!StringUtil.isBlank(idStr) && StringUtil.isBlank(brokerUrl)) {
            Integer id = Integer.parseInt(idStr);
            BrokerEntity brokerEntity = brokerService.getBroker(id);
            brokerUrl = brokerEntity.getBrokerUrl();
        }
        // get complete forward brokerEntity url
        String newUrl = brokerUrl + subStrUrl;
        // get client according url
        CloseableHttpResponse closeResponse = commonService.getCloseResponse(req, newUrl);
        commonService.writeResponse(closeResponse, res);
    }
}
