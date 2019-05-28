package com.webank.weevent.governance.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import com.webank.weevent.governance.handler.JsonAccessDeniedHandler;
import com.webank.weevent.governance.handler.JsonAuthenticationEntryPoint;
import com.webank.weevent.governance.handler.JsonLogoutSuccessHandler;
import com.webank.weevent.governance.handler.LoginFailHandler;
import com.webank.weevent.governance.properties.ConstantProperties;
import com.webank.weevent.governance.service.AccountDetailsService;

/**
 * BrowerSecurityConfig class
 * 
 * @since 2019/05/23
 *
 */
@Configuration
public class BrowerSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private AccountDetailsService userDetailService;
    @Qualifier(value = "loginSuccessHandler")
    @Autowired
    private AuthenticationSuccessHandler loginSuccessHandler;
    @Qualifier(value = "loginFailHandler")
    @Autowired
    private LoginFailHandler loginfailHandler;
    @Autowired
    private JsonAuthenticationEntryPoint jsonAuthenticationEntryPoint;
    @Autowired
    private JsonAccessDeniedHandler jsonAccessDeniedHandler;
    @Autowired
    private JsonLogoutSuccessHandler jsonLogoutSuccessHandler;
    @Autowired
    private ConstantProperties constants;

    @Override
    protected void configure(HttpSecurity http) throws Exception {

	http.exceptionHandling().accessDeniedHandler(jsonAccessDeniedHandler);

	http.formLogin() // define user login page
		// .loginPage("/login.html")
		// .loginProcessingUrl("/user/login")
		.usernameParameter("username").passwordParameter("password").permitAll()
		.successHandler(loginSuccessHandler) // if login success
		.failureHandler(loginfailHandler) // if login fail
		.and().authorizeRequests().antMatchers("/login.html", "/user/**").permitAll().anyRequest()
		.authenticated().and().csrf().disable().httpBasic()
		.authenticationEntryPoint(jsonAuthenticationEntryPoint).and().logout().logoutUrl("/user/logout")
		.deleteCookies(ConstantProperties.COOKIE_JSESSIONID, ConstantProperties.COOKIE_MGR_ACCOUNT)
		.logoutSuccessHandler(jsonLogoutSuccessHandler).permitAll();
    }

    // BrowerSecurityConfig
    @Bean
    public PasswordEncoder passwordEncoder() {
	return new BCryptPasswordEncoder();
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
	super.configure(web);
	if (!constants.getIsUseSecurity()) {
	    web.ignoring().antMatchers("/**");
	}
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
	auth.userDetailsService(userDetailService).passwordEncoder(passwordEncoder());
    }
}
