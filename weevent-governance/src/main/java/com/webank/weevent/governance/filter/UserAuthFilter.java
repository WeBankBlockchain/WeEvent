package com.webank.weevent.governance.filter;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import com.webank.weevent.governance.code.ConstantCode;
import com.webank.weevent.governance.entity.BaseResponse;
import com.webank.weevent.governance.properties.ConstantProperties;
import com.webank.weevent.governance.service.PermissionService;
import com.webank.weevent.governance.utils.CookiesTools;
import com.webank.weevent.governance.utils.JsonUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserAuthFilter implements Filter {

    @Autowired
    private CookiesTools cookiesTools;

    @Autowired
    private PermissionService permissionService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;

        String accountId = cookiesTools.getCookieValueByName(req, ConstantProperties.COOKIE_MGR_ACCOUNT_ID);
        Enumeration<String> attributeNames = request.getParameterNames();
        while (attributeNames.hasMoreElements()) {
            String requestParam = attributeNames.nextElement();
            if ("brokerId".equals(requestParam)) {
                String brokerId = request.getParameter("brokerId");
                Boolean flag = permissionService.verifyPermissions(Integer.valueOf(brokerId), accountId);
                if (!flag) {
                    BaseResponse baseResponse = new BaseResponse(ConstantCode.ACCESS_DENIED);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write(JsonUtil.toJSONString(baseResponse));
                    return;
                }
            }
        }
        chain.doFilter(request, response);
    }

}
