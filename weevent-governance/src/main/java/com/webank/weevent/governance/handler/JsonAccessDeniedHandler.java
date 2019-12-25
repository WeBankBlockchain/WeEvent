package com.webank.weevent.governance.handler;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webank.weevent.governance.code.ConstantCode;
import com.webank.weevent.governance.entity.BaseResponse;
import com.webank.weevent.governance.utils.JsonUtil;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JsonAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
        log.debug("access denied");
        BaseResponse baseResponse = new BaseResponse(ConstantCode.ACCESS_DENIED);

        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(JsonUtil.toJSONString(baseResponse));
    }

}
