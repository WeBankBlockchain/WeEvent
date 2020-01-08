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
import com.webank.weevent.governance.utils.JsonUtil;
import com.webank.weevent.governance.utils.JwtUtils;

import lombok.SneakyThrows;
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

    @SneakyThrows
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
        baseResponse.setData(JsonUtil.toJSONString(rsp));
        baseResponse.setMessage("success");
        log.debug("login backInfo:{}", JsonUtil.toJSONString(baseResponse));
        response.getWriter().write(JsonUtil.toJSONString(baseResponse));
    }

}
