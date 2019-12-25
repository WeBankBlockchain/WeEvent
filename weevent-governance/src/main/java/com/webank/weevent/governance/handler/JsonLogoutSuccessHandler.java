package com.webank.weevent.governance.handler;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webank.weevent.governance.code.ConstantCode;
import com.webank.weevent.governance.entity.BaseResponse;
import com.webank.weevent.governance.utils.CookiesTools;
import com.webank.weevent.governance.utils.JsonUtil;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JsonLogoutSuccessHandler implements LogoutSuccessHandler {

    @Autowired
    private CookiesTools cookiesTools;

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {

        // clear cookie
        cookiesTools.clearAllCookie(request, response);
        // session invaild
        request.getSession().invalidate();

        log.debug("logout success");
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);

        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(JsonUtil.toJSONString(baseResponse));
    }

}
