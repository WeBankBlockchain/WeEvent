package com.webank.weevent.governance;

import java.io.InterruptedIOException;
import java.net.UnknownHostException;

import javax.net.ssl.SSLException;

import com.webank.weevent.governance.common.GovernanceConfig;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;


/**
 * spring boot start test
 *
 * @since 2018/12/18
 */
@Slf4j
@EnableDiscoveryClient
@SpringBootApplication
@ServletComponentScan(basePackages = "com.webank.weevent.governance.filter")
public class GovernanceApplication {


    public static Environment environment;

    public static GovernanceConfig governanceConfig;

    public static void main(String[] args) throws Exception {
        SpringApplication app = new SpringApplication(GovernanceApplication.class);
        app.addListeners(new ApplicationPidFileWriter());
        app.run(args);
        log.info("Start Governance success");
    }


    @Autowired
    public void setEnvironment(org.springframework.core.env.Environment env) {
        environment = env;
    }

    @Autowired
    public void setGovernanceConfig(GovernanceConfig config) {
        governanceConfig = config;
    }

    @Bean
    public ClientHttpRequestFactory httpsClientRequestFactory(GovernanceConfig config) {
        HttpsClientRequestFactory factory = new HttpsClientRequestFactory();
        factory.setReadTimeout(config.getReadTimeout());// ms
        factory.setConnectTimeout(config.getConnectTimeOut());// ms
        return factory;
    }

    @Bean
    public ServletRegistrationBean<DispatcherServlet> weeventGovernanceServletBean(WebApplicationContext wac) {
        DispatcherServlet ds = new DispatcherServlet(wac);
        ServletRegistrationBean<DispatcherServlet> bean = new ServletRegistrationBean<>(ds, "/weevent-governance/*");
        bean.setName("weeventGovernance");
        return bean;
    }

    @Scope("prototype")
    @Bean("httpClient")
    public CloseableHttpClient getHttpClient(GovernanceConfig config) {
        /**
         * config connect parameter
         */
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();

        RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(config.getConnectionRequestTimeout())
                .setConnectTimeout(config.getHttpConnectTimeOut()).setSocketTimeout(config.getSocketTimeout()).build();

        cm.setMaxTotal(config.getMaxTotal());
        cm.setDefaultMaxPerRoute(config.getMaxPerRoute());
        CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(cm)
                .setDefaultRequestConfig(requestConfig).setRetryHandler(retryHandler).build();
        return httpClient;
    }


    /**
     * reconnet str
     */
    private HttpRequestRetryHandler retryHandler = (exception, executionCount, context) -> {
        if (executionCount >= 3) {
            // Do not retry if over max retry count
            return false;
        }
        if (exception instanceof InterruptedIOException) {
            // Timeout
            return false;
        }
        if (exception instanceof UnknownHostException) {
            // Unknown host
            return false;
        }
        if (exception instanceof ConnectTimeoutException) {
            // Connection refused
            return false;
        }
        if (exception instanceof SSLException) {
            // SSL handshake exception
            return false;
        }

        HttpClientContext clientContext = HttpClientContext.adapt(context);
        HttpRequest request = clientContext.getRequest();
        return !(request instanceof HttpEntityEnclosingRequest);
    };

}

