package com.webank.weevent.governance;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.webank.weevent.governance.filter.ForwardBrokerFilter;
import com.webank.weevent.governance.filter.ForwardProcessorFilter;
import com.webank.weevent.governance.filter.ForwardWebaseFilter;
import com.webank.weevent.governance.filter.UserAuthFilter;
import com.webank.weevent.governance.filter.XssFilter;
import com.webank.weevent.governance.handler.JsonAccessDeniedHandler;
import com.webank.weevent.governance.handler.JsonAuthenticationEntryPoint;
import com.webank.weevent.governance.handler.JsonLogoutSuccessHandler;
import com.webank.weevent.governance.handler.LoginFailHandler;
import com.webank.weevent.governance.properties.ConstantProperties;
import com.webank.weevent.governance.service.AccountDetailsService;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
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
@EnableWebSecurity
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

    //browerSecurity
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
    public WebSecurityConfigurerAdapter initBrowerSecurityConfig() {
        WebSecurityConfigurerAdapter webSecurityConfigurerAdapter = new WebSecurityConfigurerAdapter() {
            @Override
            protected void configure(HttpSecurity http) throws Exception {
                http.exceptionHandling().accessDeniedHandler(jsonAccessDeniedHandler);

                http.formLogin() // define user login page
                        .loginPage("/user/require")
                        .loginProcessingUrl("/user/login")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .permitAll()
                        .successHandler(loginSuccessHandler) // if login success
                        .failureHandler(loginfailHandler) // if login fail
                        .and()
                        .authorizeRequests()
                        .antMatchers("/user/**", "/", "/static/**", "/weevent-governance/user/**")
                        .permitAll()
                        .anyRequest()
                        .authenticated()
                        .and()
                        .csrf()
                        .disable()
                        .httpBasic()
                        .authenticationEntryPoint(jsonAuthenticationEntryPoint)
                        .and()
                        .logout()
                        .logoutUrl("/user/logout")
                        .deleteCookies(ConstantProperties.COOKIE_JSESSIONID, ConstantProperties.COOKIE_MGR_ACCOUNT)
                        .logoutSuccessHandler(jsonLogoutSuccessHandler)
                        .permitAll();
            }

            @Override
            protected void configure(AuthenticationManagerBuilder auth) throws Exception {
                auth.userDetailsService(userDetailService).passwordEncoder(new BCryptPasswordEncoder());
            }

            @Override
            public void configure(WebSecurity web) throws Exception {
                web.ignoring().antMatchers("/static/**");
            }

        };
        return webSecurityConfigurerAdapter;
    }

    @Bean
    public ClientHttpRequestFactory httpsClientRequestFactory() {
        SimpleClientHttpRequestFactory factory = initHttpsFactory();
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


    @Bean
    public SimpleClientHttpRequestFactory initHttpsFactory() {
        SimpleClientHttpRequestFactory simpleClientHttpRequestFactory = new SimpleClientHttpRequestFactory() {
            @Override
            protected void prepareConnection(HttpURLConnection connection, String httpMethod) throws IOException {
                try {
                    if (!(connection instanceof HttpsURLConnection)) {
                        throw new RuntimeException("An instance of HttpsURLConnection is expected");
                    }

                    HttpsURLConnection httpsConnection = (HttpsURLConnection) connection;

                    TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }

                        @Override
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }

                    }};

                    SSLContext sslContext = SSLContext.getInstance("TLS");
                    sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
                    httpsConnection.setSSLSocketFactory(new MyCustomSSLSocketFactory(sslContext.getSocketFactory()));

                    httpsConnection.setHostnameVerifier(new HostnameVerifier() {
                        @Override
                        public boolean verify(String s, SSLSession sslSession) {
                            return true;
                        }
                    });

                    super.prepareConnection(httpsConnection, httpMethod);
                } catch (Exception e) {
                    log.error(e.getMessage());
                }
            }


            /**
             *  We need to invoke sslSocket.setEnabledProtocols(new String[] {"SSLv3"});
             */
            class MyCustomSSLSocketFactory extends SSLSocketFactory {
                private final SSLSocketFactory delegate;

                public MyCustomSSLSocketFactory(SSLSocketFactory delegate) {
                    this.delegate = delegate;
                }

                @Override
                public String[] getDefaultCipherSuites() {
                    return delegate.getDefaultCipherSuites();
                }

                @Override
                public String[] getSupportedCipherSuites() {
                    return delegate.getSupportedCipherSuites();
                }

                @Override
                public Socket createSocket(final Socket socket, final String host, final int port, final boolean autoClose)
                        throws IOException {
                    final Socket underlyingSocket = delegate.createSocket(socket, host, port, autoClose);
                    return overrideProtocol(underlyingSocket);
                }

                @Override
                public Socket createSocket(final String host, final int port) throws IOException {
                    final Socket underlyingSocket = delegate.createSocket(host, port);
                    return overrideProtocol(underlyingSocket);
                }

                @Override
                public Socket createSocket(final String host, final int port, final InetAddress localAddress,
                                           final int localPort) throws IOException {
                    final Socket underlyingSocket = delegate.createSocket(host, port, localAddress, localPort);
                    return overrideProtocol(underlyingSocket);
                }

                @Override
                public Socket createSocket(final InetAddress host, final int port) throws IOException {
                    final Socket underlyingSocket = delegate.createSocket(host, port);
                    return overrideProtocol(underlyingSocket);
                }

                @Override
                public Socket createSocket(final InetAddress host, final int port, final InetAddress localAddress,
                                           final int localPort) throws IOException {
                    final Socket underlyingSocket = delegate.createSocket(host, port, localAddress, localPort);
                    return overrideProtocol(underlyingSocket);
                }

                private Socket overrideProtocol(final Socket socket) {
                    if (!(socket instanceof SSLSocket)) {
                        throw new RuntimeException("An instance of SSLSocket is expected");
                    }
                    ((SSLSocket) socket).setEnabledProtocols(new String[]{"TLSv1"});
                    return socket;
                }
            }
        };
        return simpleClientHttpRequestFactory;
    }
}

