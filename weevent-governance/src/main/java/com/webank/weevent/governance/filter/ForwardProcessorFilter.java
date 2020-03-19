package com.webank.weevent.governance.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webank.weevent.governance.service.CommonService;
import com.webank.weevent.governance.utils.Utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;

@Slf4j
@WebFilter(urlPatterns = "/weevent-processor/*")
public class ForwardProcessorFilter implements Filter {
    private final static String processorServiceId = "weevent-processor";

    private CommonService commonService;

    private DiscoveryClient discoveryClient;

    @Autowired
    public void setCommonService(CommonService commonService) {
        this.commonService = commonService;
    }

    @Autowired(required = false)
    public void setDiscoveryClient(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        String originUrl = req.getRequestURI();
        // get tail of processor url
        String subStrUrl = originUrl.substring(originUrl.indexOf("/weevent-processor/") + "/weevent-processor".length());
        // get complete forward processor url
        String uri = Utils.getUrlFromDiscovery(this.discoveryClient, processorServiceId);
        if (uri.isEmpty()) {
            log.error("unknown processor url");
            throw new IOException("unknown processor url");
        }
        String newUrl = uri + "/" + processorServiceId + subStrUrl;
        // get client according url
        CloseableHttpResponse closeResponse = commonService.getCloseResponse(req, newUrl);
        commonService.writeResponse(closeResponse, res);
    }
}
