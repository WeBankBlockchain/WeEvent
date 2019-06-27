package com.webank.weevent.governance.configuration;

import java.io.InterruptedIOException;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import lombok.extern.slf4j.Slf4j;

/**
 * http connect pool cofigure
 * 
 * @date 2018/04/30
 */
@Configuration
@Slf4j
public class ConnectionManager {

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

    private PoolingHttpClientConnectionManager cm;

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
        boolean idempotent = !(request instanceof HttpEntityEnclosingRequest);
        if (idempotent) {
            // Retry if the request is considered idempotent
            return true;
        }
        return false;
    };

    /**
     * config connect parameter
     */
    private RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(connectionRequestTimeout)
            .setConnectTimeout(connectionTimeout).setSocketTimeout(socketTimeout).build();

    public ConnectionManager() {
        cm = new PoolingHttpClientConnectionManager();
    }

    @Bean("httpClient")
    public CloseableHttpClient getHttpClient() {
        cm.setMaxTotal(maxTotal);
        cm.setDefaultMaxPerRoute(maxPerRoute);
        CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(cm)
                .setDefaultRequestConfig(requestConfig).setRetryHandler(retryHandler).build();
        return httpClient;
    }

    @Bean("httpsClient")
    public CloseableHttpClient getHttpsClient() {
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.INSTANCE).register("https", trustAllHttpsCertificates())
                .build();
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(
                socketFactoryRegistry);
        CloseableHttpClient httpsClient = HttpClients.custom().setConnectionManager(connectionManager).build();
        return httpsClient;
    }

    private SSLConnectionSocketFactory trustAllHttpsCertificates() {
        SSLConnectionSocketFactory socketFactory = null;
        TrustManager[] trustAllCerts = new TrustManager[1];
        TrustManager tm = new miTM();
        trustAllCerts[0] = tm;
        SSLContext sc = null;
        try {
            sc = SSLContext.getInstance("TLS");// sc = SSLContext.getInstance("TLS")
            sc.init(null, trustAllCerts, null);
            socketFactory = new SSLConnectionSocketFactory(sc, NoopHostnameVerifier.INSTANCE);
            // HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (NoSuchAlgorithmException e) {
            log.error(e.getMessage());
        } catch (KeyManagementException e) {
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
