package com.webank.weevent.governance.filter;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webank.weevent.governance.GovernanceApplication;
import com.webank.weevent.governance.handler.LoginFailHandler;
import com.webank.weevent.governance.utils.JwtUtils;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private AuthenticationManager authenticationManager;

    private AuthenticationSuccessHandler loginSuccessHandler;

    private LoginFailHandler loginFailHandler;

    public LoginFilter(AuthenticationManager authenticationManager, AuthenticationSuccessHandler loginSuccessHandler, LoginFailHandler loginFailHandler) {
        this.authenticationManager = authenticationManager;
        this.loginSuccessHandler = loginSuccessHandler;
        this.loginFailHandler = loginFailHandler;
        setFilterProcessesUrl("/user/login");
    }

    @Override
    public void setFilterProcessesUrl(String filterProcessesUrl) {
        super.setFilterProcessesUrl(filterProcessesUrl);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        return authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password, new ArrayList<>()));
    }

    // after the user successfully logs in, this method will be called, and we generate a token in this method
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
                                            Authentication authResult) throws IOException, ServletException {
        String username = ((User) authResult.getPrincipal()).getUsername();
        String token = JwtUtils.encodeToken(username, GovernanceApplication.governanceConfig.getPrivateSecret(), JwtUtils.EXPIRE_TIME);
        response.addHeader(JwtUtils.AUTHORIZATION_HEADER_PREFIX, token);
        loginSuccessHandler.onAuthenticationSuccess(request, response, authResult);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        loginFailHandler.onAuthenticationFailure(request,response,failed);
    }
}