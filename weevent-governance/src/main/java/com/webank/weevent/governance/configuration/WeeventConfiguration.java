package com.webank.weevent.governance.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import com.webank.weevent.governance.filter.ForwardWebaseFilter;
import com.webank.weevent.governance.filter.ForwardBrokerFilter;
import com.webank.weevent.governance.filter.XssFilter;

@Configuration
public class WeeventConfiguration {

    @Autowired
    private ForwardBrokerFilter forwardBrokerFilter;

    @Autowired
    private ForwardWebaseFilter forwardWebaseFilter;

    @Bean
    public ClientHttpRequestFactory httpsClientRequestFactory() {
	HttpsClientRequestFactory factory = new HttpsClientRequestFactory();
	factory.setReadTimeout(5000);// ms
	factory.setConnectTimeout(15000);// ms
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
    public FilterRegistrationBean<ForwardBrokerFilter> httpForwardFilterRegistrationBean() {
	FilterRegistrationBean<ForwardBrokerFilter> filterRegistrationBean = new FilterRegistrationBean<>();
	filterRegistrationBean.setFilter(forwardBrokerFilter);
	filterRegistrationBean.setOrder(2);
	filterRegistrationBean.setEnabled(true);
	filterRegistrationBean.addUrlPatterns("/weevent-governance/weevent/*");
	return filterRegistrationBean;
    }

    @Bean
    public FilterRegistrationBean<ForwardWebaseFilter> forwardWebaseFilterRegistrationBean() {
	FilterRegistrationBean<ForwardWebaseFilter> filterRegistrationBean = new FilterRegistrationBean<>();
	filterRegistrationBean.setFilter(forwardWebaseFilter);
	filterRegistrationBean.setOrder(3);
	filterRegistrationBean.setEnabled(true);
	filterRegistrationBean.addUrlPatterns("/weevent-governance/webase-node-mgr/*");
	return filterRegistrationBean;
    }

    @Bean
    public FilterRegistrationBean<XssFilter> xssFilterRegistrationBean() {
	FilterRegistrationBean<XssFilter> filterRegistrationBean = new FilterRegistrationBean<>();
	filterRegistrationBean.setFilter(new XssFilter());
	filterRegistrationBean.setOrder(1);
	filterRegistrationBean.setEnabled(true);
	filterRegistrationBean.addUrlPatterns("/weevent-governance/topic/*");
	return filterRegistrationBean;
    }
}
