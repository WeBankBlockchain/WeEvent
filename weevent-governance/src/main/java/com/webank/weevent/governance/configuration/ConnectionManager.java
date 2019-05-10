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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * http connect pool cofigure
 * @date 2018/04/30
 */
@Configuration
public class ConnectionManager {

   // max connect
   private static final int MAX_TOTAL = 200;
   private static final int MAX_PER_ROUTE = 500;
   private static final int CONNECTION_REQUEST_TIMEOUT = 3000;
   private static final int CONNECTION_TIMEOUT = 3000;
   private static final int SOCKET_TIMEOUT = 5000;

   private PoolingHttpClientConnectionManager cm;
   private CloseableHttpClient httpClient;

   /**
    * reconnet str
    */
   HttpRequestRetryHandler retryHandler = (exception, executionCount,context) -> {
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
   RequestConfig requestConfig = RequestConfig.custom()
       .setConnectionRequestTimeout(CONNECTION_REQUEST_TIMEOUT)
       .setConnectTimeout(CONNECTION_TIMEOUT)
       .setSocketTimeout(SOCKET_TIMEOUT)
       .build();

   public ConnectionManager() {
       cm = new PoolingHttpClientConnectionManager();
       cm.setMaxTotal(MAX_TOTAL);
       cm.setDefaultMaxPerRoute(MAX_PER_ROUTE);

       httpClient = HttpClients.custom()
           .setConnectionManager(cm)
           .setDefaultRequestConfig(requestConfig)
           .setRetryHandler(retryHandler)
           .build();
   }

   @Bean("httpClient")
   public CloseableHttpClient getHttpClient() {
       return httpClient;
   }

   @Bean("httpsClient")
   public CloseableHttpClient getHttpsClient() {
	   Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create().register("http", PlainConnectionSocketFactory.INSTANCE).register("https", trustAllHttpsCertificates()).build();
		PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
		CloseableHttpClient httpsClient = HttpClients.custom().setConnectionManager(connectionManager).build();
		return httpsClient;
   }
   
   private  SSLConnectionSocketFactory trustAllHttpsCertificates() {
		SSLConnectionSocketFactory socketFactory = null;
		TrustManager[] trustAllCerts = new TrustManager[1];
		TrustManager tm = new miTM();
		trustAllCerts[0] = tm;
		SSLContext sc = null;
		try {
			sc = SSLContext.getInstance("TLS");//sc = SSLContext.getInstance("TLS")
			sc.init(null, trustAllCerts, null);
			socketFactory = new SSLConnectionSocketFactory(sc, NoopHostnameVerifier.INSTANCE);
			//HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (KeyManagementException e) {
			e.printStackTrace();
		}
		return socketFactory;
	}
	
	private class miTM implements TrustManager, X509TrustManager {
		
		public X509Certificate[] getAcceptedIssuers() {
			return null;
		}
		
		public void checkServerTrusted(X509Certificate[] certs, String authType) {
			//don't check
		}
		
		public void checkClientTrusted(X509Certificate[] certs, String authType) {
			//don't check
		}
	}
}

