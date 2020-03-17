package com.webank.weevent.governance.handler;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webank.weevent.client.BrokerException;
import com.webank.weevent.client.JsonHelper;
import com.webank.weevent.governance.common.ConstantCode;
import com.webank.weevent.governance.entity.BaseResponse;
import com.webank.weevent.governance.utils.JwtUtils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JsonLogoutSuccessHandler implements LogoutSuccessHandler {


    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {
        //clear token
        request.setAttribute(JwtUtils.AUTHORIZATION_HEADER_PREFIX, null);
        SecurityContextHolder.getContext().setAuthentication(null);
        log.debug("logout success");
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);

        response.setContentType("application/json;charset=UTF-8");
        try {
            response.getWriter().write(JsonHelper.object2Json(baseResponse));
        } catch (BrokerException e) {
            log.error("Code: " + e.getCode() + ", " + e.getMessage());
        }
    }

}
