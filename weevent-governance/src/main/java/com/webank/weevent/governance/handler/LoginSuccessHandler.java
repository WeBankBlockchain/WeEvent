package com.webank.weevent.governance.handler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webank.weevent.governance.code.ConstantCode;
import com.webank.weevent.governance.entity.Account;
import com.webank.weevent.governance.entity.BaseResponse;
import com.webank.weevent.governance.properties.ConstantProperties;
import com.webank.weevent.governance.service.AccountService;
import com.webank.weevent.governance.utils.CookiesTools;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component("loginSuccessHandler")
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private AccountService accountService;

    @Autowired
    private CookiesTools cookiesTools;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        log.debug("login success");

        Object obj = authentication.getPrincipal();
        JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(obj));
        String username = jsonObject.getString("username");
        // response account info
        Account account = accountService.queryByUsername(username);
        Map<String, Object> rsp = new HashMap<>();
        rsp.put("username", username);
        rsp.put("userId", account.getId());
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        baseResponse.setData(rsp);

        Integer userId = account.getId();
        // clear cookie
        cookiesTools.clearAllCookie(request, response);
        // reset session
        request.getSession().invalidate();
        request.getSession().setAttribute(ConstantProperties.SESSION_MGR_ACCOUNT, username);

        // reset cookie
        cookiesTools.addCookie(request, response, ConstantProperties.COOKIE_MGR_ACCOUNT, username);
        cookiesTools.addCookie(request, response, ConstantProperties.COOKIE_MGR_ACCOUNT_ID, userId.toString());
        cookiesTools.addCookie(request, response, ConstantProperties.COOKIE_JSESSIONID, request.getSession().getId());

        String backStr = JSON.toJSONString(baseResponse);
        log.debug("login backInfo:{}", backStr);

        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(backStr);
    }

}
