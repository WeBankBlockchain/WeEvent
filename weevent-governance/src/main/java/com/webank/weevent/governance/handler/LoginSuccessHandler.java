package com.webank.weevent.governance.handler;

import java.io.IOException;
import java.security.Security;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webank.weevent.governance.common.ConstantCode;
import com.webank.weevent.governance.entity.AccountEntity;
import com.webank.weevent.governance.entity.BaseResponse;
import com.webank.weevent.governance.service.AccountService;
import com.webank.weevent.governance.utils.JwtUtils;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.JsonHelper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component("loginSuccessHandler")
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private AccountService accountService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        log.debug("login success");
        Map<String, Object> rsp = new HashMap<>();
        User principal = (User) authentication.getPrincipal();
        AccountEntity accountEntity = accountService.queryByUsername(principal.getUsername());
        String authorization = response.getHeader(JwtUtils.AUTHORIZATION_HEADER_PREFIX);
        //Set the global user Id variable
        Security.setProperty(authorization, accountEntity.getId().toString());
        rsp.put(JwtUtils.AUTHORIZATION_HEADER_PREFIX, authorization);
        rsp.put("username", accountEntity.getUsername());
        //return
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        try {
            baseResponse.setData(JsonHelper.object2Json(rsp));
            baseResponse.setMessage("success");
            log.debug("login backInfo:{}", JsonHelper.object2Json(baseResponse));
            response.getWriter().write(JsonHelper.object2Json(baseResponse));
        } catch (BrokerException e) {
            log.error("Code: " + e.getCode() + ", " + e.getMessage());
        }
    }

}
