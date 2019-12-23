package com.webank.weevent.governance.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webank.weevent.governance.service.CommonService;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ForwardProcessorFilter implements Filter {


    @Autowired
    private CommonService commonService;

    @Value("${weevent.processor.url:http://127.0.0.1:7008}")
    private String weeventProcessorUrl;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        String originUrl = req.getRequestURI();
        // get tail of processor url
        String subStrUrl = originUrl.substring(originUrl.indexOf("/processor/") + "/processor".length());
        // get complete forward processor url
        String newUrl = weeventProcessorUrl + "/processor" + subStrUrl;
        // get client according url
        CloseableHttpResponse closeResponse = commonService.getCloseResponse(req, newUrl);
        commonService.writeResponse(closeResponse, res);
    }
}
