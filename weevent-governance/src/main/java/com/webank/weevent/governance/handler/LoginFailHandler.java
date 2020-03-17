package com.webank.weevent.governance.handler;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webank.weevent.client.BrokerException;
import com.webank.weevent.client.JsonHelper;
import com.webank.weevent.governance.common.ConstantCode;
import com.webank.weevent.governance.entity.BaseResponse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component("loginFailHandler")
public class LoginFailHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException ex) throws IOException {
        log.info("login fail", ex);
        BaseResponse baseResponse = new BaseResponse(ConstantCode.LOGIN_FAIL);
        baseResponse.setMessage(ex.getMessage());
        response.setContentType("application/json;charset=UTF-8");
        try {
            response.getWriter().write(JsonHelper.object2Json(baseResponse));
        } catch (BrokerException e) {
            log.error("Code: " + e.getCode() + ", " + e.getMessage());
        }

    }
}
