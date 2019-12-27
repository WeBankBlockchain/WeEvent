package com.webank.weevent.governance;

import java.io.InterruptedIOException;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.webank.weevent.governance.filter.ForwardBrokerFilter;
import com.webank.weevent.governance.filter.ForwardProcessorFilter;
import com.webank.weevent.governance.filter.ForwardWebaseFilter;
import com.webank.weevent.governance.filter.UserAuthFilter;
import com.webank.weevent.governance.filter.XssFilter;
import com.webank.weevent.governance.utils.H2ServerUtil;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;


/**
 * spring boot start test
 *
 * @since 2018/12/18
 */
@Slf4j
@SpringBootApplication
@EnableTransactionManagement
public class GovernanceApplication {

    @Value("${https.read-timeout:3000}")
    private int readTimeout;

    @Value("${https.connect-timeout:3000}")
    private int connectTimeOut;

    // max connect
    @Value("${http.client.max-total:200}")
    private int maxTotal;

    @Value("${http.client.max-per-route:500}")
    private int maxPerRoute;

    @Value("${http.client.connection-request-timeout:3000}")
    private int connectionRequestTimeout;

    @Value("${http.client.connection-timeout:3000}")
    private int connectionTimeout;

    @Value("${http.client.socket-timeout:5000}")
    private int socketTimeout;

    @Autowired
    private ForwardBrokerFilter forwardBrokerFilter;

    @Autowired
    private ForwardWebaseFilter forwardWebaseFilter;

    @Autowired
    private UserAuthFilter userAuthFilter;

    @Autowired
    private ForwardProcessorFilter forwardProcessorFilter;

    private PoolingHttpClientConnectionManager cm;


    public static void main(String[] args) throws Exception {
        H2ServerUtil.startH2();
        SpringApplication app = new SpringApplication(GovernanceApplication.class);
        app.addListeners(new ApplicationPidFileWriter());
        app.run(args);
        log.info("Start Governance success");
    }


    public GovernanceApplication() {
        cm = new PoolingHttpClientConnectionManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

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


    @Bean
    public FilterRegistrationBean<ForwardProcessorFilter> forwardProcessorRegistrationBean() {
        FilterRegistrationBean<ForwardProcessorFilter> filterRegistrationBean = new FilterRegistrationBean<>();
        filterRegistrationBean.setFilter(forwardProcessorFilter);
        filterRegistrationBean.setOrder(5);
        filterRegistrationBean.setEnabled(true);
        filterRegistrationBean.addUrlPatterns("/weevent-governance/processor/*");
        return filterRegistrationBean;
    }


    @Scope("prototype")
    @Bean("httpClient")
    public CloseableHttpClient getHttpClient() {
        cm.setMaxTotal(maxTotal);
        cm.setDefaultMaxPerRoute(maxPerRoute);
        CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(cm)
                .setDefaultRequestConfig(requestConfig).setRetryHandler(retryHandler).build();
        return httpClient;
    }

    @Scope("prototype")
    @Bean("httpsClient")
    public CloseableHttpClient getHttpsClient() {
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.INSTANCE).register("https", trustAllHttpsCertificates())
                .build();
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(
                socketFactoryRegistry);
        CloseableHttpClient httpsClient = HttpClients.custom().setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig).setRetryHandler(retryHandler).build();
        return httpsClient;
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

    /**
     * config connect parameter
     */
    private RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(connectionRequestTimeout)
            .setConnectTimeout(connectionTimeout).setSocketTimeout(socketTimeout).build();


    private SSLConnectionSocketFactory trustAllHttpsCertificates() {
        SSLConnectionSocketFactory socketFactory = null;
        TrustManager[] trustAllCerts = new TrustManager[1];
        TrustManager tm = null;
        // TrustManager tm1 = this.miTM;
        trustAllCerts[0] = tm;
        SSLContext sc = null;
        try {
            sc = SSLContext.getInstance("TLS");// sc = SSLContext.getInstance("TLS")
            sc.init(null, trustAllCerts, null);
            socketFactory = new SSLConnectionSocketFactory(sc, NoopHostnameVerifier.INSTANCE);
            // HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            log.error(e.getMessage());
        }
        return socketFactory;
    }

    private class miTM implements TrustManager, X509TrustManager {

        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        public void checkServerTrusted(X509Certificate[] certs, String authType) {
            // don't check
        }

        public void checkClientTrusted(X509Certificate[] certs, String authType) {
            // don't check
        }
    }
}

