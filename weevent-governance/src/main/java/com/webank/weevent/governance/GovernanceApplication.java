package com.webank.weevent.governance;

import com.webank.weevent.governance.common.GovernanceConfig;
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
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.InterruptedIOException;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;


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


    private PoolingHttpClientConnectionManager cm;

    public static Environment environment;

    public static GovernanceConfig governanceConfig;

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


    @Autowired
    public void setEnvironment(org.springframework.core.env.Environment env) {
        environment = env;
    }

    @Autowired
    public void setGovernanceConfig(GovernanceConfig config) {
        governanceConfig = config;
    }

    @Bean
    public ClientHttpRequestFactory httpsClientRequestFactory() {
        HttpsClientRequestFactory factory = new HttpsClientRequestFactory();
        factory.setReadTimeout(governanceConfig.getReadTimeout());// ms
        factory.setConnectTimeout(governanceConfig.getConnectTimeOut());// ms
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
    @ConditionalOnProperty(prefix = "http", name = "connect-timeout", havingValue = "3000")
    public CloseableHttpClient getHttpClient() {
        /**
         * config connect parameter
         */
        RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(governanceConfig.getConnectionRequestTimeout())
                .setConnectTimeout(governanceConfig.getHttpConnectTimeOut()).setSocketTimeout(governanceConfig.getSocketTimeout()).build();

        cm.setMaxTotal(governanceConfig.getMaxTotal());
        cm.setDefaultMaxPerRoute(governanceConfig.getMaxPerRoute());
        CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(cm)
                .setDefaultRequestConfig(requestConfig).setRetryHandler(retryHandler).build();
        return httpClient;
    }

    @Scope("prototype")
    @Bean("httpsClient")
    @ConditionalOnProperty(prefix = "https", name = "connect-timeout", havingValue = "3000")
    public CloseableHttpClient getHttpsClient() {
        RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(governanceConfig.getConnectionRequestTimeout())
                .setConnectTimeout(governanceConfig.getConnectTimeOut()).setSocketTimeout(governanceConfig.getSocketTimeout()).build();

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

