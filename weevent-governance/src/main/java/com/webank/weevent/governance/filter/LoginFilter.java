package com.webank.weevent.governance.filter;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webank.weevent.governance.entity.AccountEntity;
import com.webank.weevent.governance.utils.JwtUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private AuthenticationManager authenticationManager;

    private AuthenticationSuccessHandler loginSuccessHandler;


    public LoginFilter(AuthenticationManager authenticationManager, AuthenticationSuccessHandler loginSuccessHandler) {
        this.authenticationManager = authenticationManager;
        this.loginSuccessHandler = loginSuccessHandler;
    }


    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        try {
            AccountEntity user = new ObjectMapper().readValue(request.getInputStream(), AccountEntity.class);
            return authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword(), new ArrayList<>()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // 用户成功登录后，这个方法会被调用，我们在这个方法里生成token
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
                                            Authentication authResult) throws IOException, ServletException {
        String token = JwtUtils.createToken(authResult);
        response.addHeader(JwtUtils.AUTHORIZATION_HEADER_PREFIX, token);
        loginSuccessHandler.onAuthenticationSuccess(request, response, authResult);
    }

}