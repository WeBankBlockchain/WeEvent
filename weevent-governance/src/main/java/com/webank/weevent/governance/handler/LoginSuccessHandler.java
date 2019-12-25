package com.webank.weevent.governance.handler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webank.weevent.governance.code.ConstantCode;
import com.webank.weevent.governance.entity.AccountEntity;
import com.webank.weevent.governance.entity.BaseResponse;
import com.webank.weevent.governance.properties.ConstantProperties;
import com.webank.weevent.governance.service.AccountService;
import com.webank.weevent.governance.utils.CookiesTools;
import com.webank.weevent.governance.utils.JsonUtil;

import lombok.SneakyThrows;
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

    @SneakyThrows
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        log.debug("login success");

        Object obj = authentication.getPrincipal();
        Map map = JsonUtil.parseObject(JsonUtil.toJSONString(obj), Map.class);
        String username = (String) map.get("username");
        // response accountEntity info
        AccountEntity accountEntity = accountService.queryByUsername(username);
        Map<String, Object> rsp = new HashMap<>();
        rsp.put("username", username);
        rsp.put("userId", accountEntity.getId());
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        baseResponse.setData(rsp);

        Integer userId = accountEntity.getId();
        // clear cookie
        cookiesTools.clearAllCookie(request, response);
        // reset session
        request.getSession().invalidate();
        request.getSession().setAttribute(ConstantProperties.SESSION_MGR_ACCOUNT, username);

        // reset cookie
        cookiesTools.addCookie(request, response, ConstantProperties.COOKIE_MGR_ACCOUNT, username);
        cookiesTools.addCookie(request, response, ConstantProperties.COOKIE_MGR_ACCOUNT_ID, userId.toString());
        cookiesTools.addCookie(request, response, ConstantProperties.COOKIE_JSESSIONID, request.getSession().getId());

        String backStr = JsonUtil.toJSONString(baseResponse);
        log.debug("login backInfo:{}", backStr);

        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(backStr);
    }

}
