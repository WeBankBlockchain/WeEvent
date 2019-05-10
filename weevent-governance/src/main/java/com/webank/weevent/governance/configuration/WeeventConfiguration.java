package com.webank.weevent.governance.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import com.webank.weevent.governance.filter.HttpForwardFilter;
import com.webank.weevent.governance.filter.XssFilter;

@Configuration
public class WeeventConfiguration {
    
    @Value("${governance.influxdb.enabled}")
    private String enabled;
    @Value("${governance.influxdb.username}")
    private String username;
    @Value("${governance.influxdb.password}")
    private String password;
    @Value("${governance.influxdb.openurl}")
    private String openurl;
    @Value("${governance.influxdb.database}")
    private String database;
    
    @Value("${weevent.url}")
    private String weeventUrl;
    
    @Autowired
    private HttpForwardFilter httpForwardFilter;
     
    @Bean
    public InfluxDBConnect getInfluxDBConnect(){
        InfluxDBConnect influxDB = new InfluxDBConnect(enabled,username, password, openurl, database);
      if(enabled != null && enabled.equals("true")) {
          influxDB.influxDbBuild();
          influxDB.createRetentionPolicy();
      }
      return influxDB;
    }


    /**
     * get RestTemplate Bean
     * @param factory
     * @return
     */
    @Bean
    public RestTemplate restTemplate(ClientHttpRequestFactory factory) {
        if(weeventUrl.startsWith("https")) {
            return new RestTemplate(factory);
        }else {
            return new RestTemplate();
        }
    }

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
    public FilterRegistrationBean<HttpForwardFilter> httpForwardFilterRegistrationBean() {
        FilterRegistrationBean<HttpForwardFilter> filterRegistrationBean = new FilterRegistrationBean<>();
        filterRegistrationBean.setFilter(httpForwardFilter);
        filterRegistrationBean.setOrder(2);
        filterRegistrationBean.setEnabled(true);
        filterRegistrationBean.addUrlPatterns("/weevent-governance/weevent/*");
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
