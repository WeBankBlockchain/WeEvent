package com.webank.weevent.governance.configuration;

import com.webank.weevent.governance.filter.ForwardWebaseFilter;
import com.webank.weevent.governance.filter.UserAuthFilter;
import com.webank.weevent.governance.filter.ForwardBrokerFilter;
import com.webank.weevent.governance.filter.XssFilter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

@Configuration
public class WeeventConfiguration {

    @Value("${https.read-timeout:3000}")
    private int readTimeout;

    @Value("${https.connect-timeout:3000}")
    private int connectTimeOut;

    @Autowired
    private ForwardBrokerFilter forwardBrokerFilter;

    @Autowired
    private ForwardWebaseFilter forwardWebaseFilter;

    @Autowired
    private UserAuthFilter userAuthFilter;

    @Bean
    public ClientHttpRequestFactory httpsClientRequestFactory() {
        HttpsClientRequestFactory factory = new HttpsClientRequestFactory();
        factory.setReadTimeout(readTimeout);// ms
        factory.setConnectTimeout(connectTimeOut);// ms
        return factory;
    }

    @Bean
    public ServletRegistrationBean<DispatcherServlet> weeventGovernanceServletBean(WebApplicationContext wac) {
        DispatcherServlet ds = new DispatcherServlet(wac);
        ServletRegistrationBean<DispatcherServlet> bean = new ServletRegistrationBean<>(ds, "/weevent-governance/*");
        bean.setName("weeventGovernance");
        return bean;
    }

    @Bean
    public FilterRegistrationBean<UserAuthFilter> userAuthFilterRegistrationBean() {
        FilterRegistrationBean<UserAuthFilter> filterRegistrationBean = new FilterRegistrationBean<>();
        filterRegistrationBean.setFilter(userAuthFilter);
        filterRegistrationBean.setOrder(1);
        filterRegistrationBean.setEnabled(true);
        filterRegistrationBean.addUrlPatterns("/weevent-governance/*");
        return filterRegistrationBean;
    }

    @Bean
    public FilterRegistrationBean<XssFilter> xssFilterRegistrationBean() {
        FilterRegistrationBean<XssFilter> filterRegistrationBean = new FilterRegistrationBean<>();
        filterRegistrationBean.setFilter(new XssFilter());
        filterRegistrationBean.setOrder(2);
        filterRegistrationBean.setEnabled(true);
        filterRegistrationBean.addUrlPatterns("/weevent-governance/topic/*");
        return filterRegistrationBean;
    }

    @Bean
    public FilterRegistrationBean<ForwardBrokerFilter> httpForwardFilterRegistrationBean() {
        FilterRegistrationBean<ForwardBrokerFilter> filterRegistrationBean = new FilterRegistrationBean<>();
        filterRegistrationBean.setFilter(forwardBrokerFilter);
        filterRegistrationBean.setOrder(3);
        filterRegistrationBean.setEnabled(true);
        filterRegistrationBean.addUrlPatterns("/weevent-governance/weevent/*");
        return filterRegistrationBean;
    }

    @Bean
    public FilterRegistrationBean<ForwardWebaseFilter> forwardWebaseFilterRegistrationBean() {
        FilterRegistrationBean<ForwardWebaseFilter> filterRegistrationBean = new FilterRegistrationBean<>();
        filterRegistrationBean.setFilter(forwardWebaseFilter);
        filterRegistrationBean.setOrder(4);
        filterRegistrationBean.setEnabled(true);
        filterRegistrationBean.addUrlPatterns("/weevent-governance/webase-node-mgr/*");
        return filterRegistrationBean;
    }

}
