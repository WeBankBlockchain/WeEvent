package com.webank.weevent.governance.handler;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webank.weevent.client.BrokerException;
import com.webank.weevent.client.JsonHelper;
import com.webank.weevent.governance.common.ConstantCode;
import com.webank.weevent.governance.entity.BaseResponse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JsonAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        log.debug("user not logged in");
        BaseResponse baseResponse = new BaseResponse(ConstantCode.USER_NOT_LOGGED_IN);

        response.setContentType("application/json;charset=UTF-8");
        try {
            response.getWriter().write(JsonHelper.object2Json(baseResponse));
        } catch (BrokerException e) {
            log.error("Code: " + e.getCode() + ", " + e.getMessage());
        }
    }

}
