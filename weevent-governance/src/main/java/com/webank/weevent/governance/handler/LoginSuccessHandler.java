package com.webank.weevent.governance.handler;

import com.webank.weevent.governance.entity.AccountEntity;
import com.webank.weevent.governance.service.AccountService;
import com.webank.weevent.governance.utils.JsonUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Security;
import java.util.HashMap;
import java.util.Map;

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
        String authorization = response.getHeader("Authorization");
        //Set the global user Id variable
        Security.setProperty(authorization,accountEntity.getId().toString());
        rsp.put("access_token",authorization);
        String backStr = JsonUtil.toJSONString(rsp);
        log.debug("login backInfo:{}", backStr);
        response.getWriter().write(backStr);
    }

}
