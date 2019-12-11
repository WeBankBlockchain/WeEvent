package com.webank.weevent;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.Executor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webank.weevent.broker.config.WeEventConfig;
import com.webank.weevent.broker.ha.MasterJob;
import com.webank.weevent.broker.plugin.IConsumer;
import com.webank.weevent.broker.plugin.IProducer;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.WeEvent;

import com.fasterxml.jackson.databind.JsonNode;
import com.googlecode.jsonrpc4j.ErrorResolver;
import com.googlecode.jsonrpc4j.spring.AutoJsonRpcServiceImplExporter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.coyote.http11.Http11NioProtocol;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author websterchen
 * @version 1.0
 * @since 2018/12/13
 */
@Slf4j
class HttpInterceptor implements HandlerInterceptor {
    private String ipWhiteTable;

    HttpInterceptor(String ipWhiteTable) {
        this.ipWhiteTable = ipWhiteTable;
    }

    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        log.debug("HttpServletRequest [X-Forwarded-For]: {}", ip);
        if (StringUtils.isBlank(ip) || ip.equalsIgnoreCase("unknown")) {
            log.debug("HttpServletRequest [getRemoteAddr]: {}", ip);
            return request.getRemoteAddr();
        }

        String[] ips = ip.split(",");
        for (String strIp : ips) {
            if (!strIp.equalsIgnoreCase("unknown")) {
                return strIp;
            }
        }

        return "";
    }

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) {
        String ip = getIpAddress(httpServletRequest);
        if (this.ipWhiteTable.equals("")) {
            return true;
        }
        log.debug("ip white list:{} client ip:{}", this.ipWhiteTable, ip);
        if (ip.contains("0:0:0:0") || ip.contains("127.0.0.1") || ip.contains("localhost")) {
            return true;
        }
        if (!this.ipWhiteTable.contains(ip)) {
            log.error("forbid,client ip:{} not in white table:{}", ip, this.ipWhiteTable);
            httpServletResponse.setStatus(403);
            return false;
        }
        return true;
    }
}

/**
 * @author websterchen
 * @version 1.0
 * @since 2018/12/13
 */
@Slf4j
class HttpInterceptorConfig implements WebMvcConfigurer {
    @Bean
    public HttpInterceptor interceptor() {
        log.info("client ip white table: {}", BrokerApplication.weEventConfig.getIpWhiteTable());
        return new HttpInterceptor(BrokerApplication.weEventConfig.getIpWhiteTable());
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(interceptor()).addPathPatterns("/**");
    }
}


/**
 * Main entrance for broker application.
 *
 * @author matthewliu
 * @since 2018/11/21
 */
@Slf4j
@SpringBootApplication
public class BrokerApplication {
    public static ApplicationContext applicationContext;

    public static WeEventConfig weEventConfig;

    public static Environment environment;

    public static void main(String[] args) {
        /* Forbid banner.
        SpringApplicationBuilder builder = new SpringApplicationBuilder(BrokerApplication.class);
        builder.bannerMode(Banner.Mode.OFF).run(args);
        */
        SpringApplication app = new SpringApplication(BrokerApplication.class);
        app.addListeners(new ApplicationPidFileWriter());
        app.run();
        log.info("read from weevent.properties, {}", weEventConfig);
        log.info("start broker success");
        //spring-boot-starter-actuator customize /info with InfoContributor bean
    }

    @Autowired
    public void setContext(ApplicationContext context) {
        applicationContext = context;
    }

    @Autowired
    public void setWeEventConfig(WeEventConfig config) {
        weEventConfig = config;
    }

    @Autowired
    public void setEnvironment(Environment env) {
        environment = env;
    }

    public static void exit() {
        if (applicationContext != null) {
            System.exit(SpringApplication.exit(applicationContext));
        } else {
            System.exit(1);
        }
    }

    // tomcat configuration to enhance performance
    @Bean
    public ConfigurableServletWebServerFactory configurableServletWebServerFactory() {
        log.info("custom TomcatServletWebServerFactory");

        TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
        factory.setProtocol(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);

        factory.addConnectorCustomizers((connector) -> {
                    // default max connections = 10000
                    // default connection timeout = 20000
                    // default max accept count = 100
                    // default max worker thread = 200
                    connector.setEnableLookups(false);
                    connector.setAllowTrace(false);

                    Http11NioProtocol http11NioProtocol = (Http11NioProtocol) connector.getProtocolHandler();
                    http11NioProtocol.setKeepAliveTimeout(60000);
                    http11NioProtocol.setMaxKeepAliveRequests(10000);
                    http11NioProtocol.setDisableUploadTimeout(true);
                    http11NioProtocol.setTcpNoDelay(true);
                }
        );

        return factory;
    }

    //support CORS
    @Bean
    public WebMvcConfigurer webMvcConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedHeaders("*")
                        .allowedMethods("*")
                        .allowedOrigins("*");
            }
        };
    }

    // export jsonrpc4j
    @Bean
    public static AutoJsonRpcServiceImplExporter autoJsonRpcServiceImplExporter() {
        //in here you can provide custom HTTP status code providers etc. eg:
        //exporter.setHttpStatusCodeProvider();

        AutoJsonRpcServiceImplExporter exporter = new AutoJsonRpcServiceImplExporter();
        exporter.setErrorResolver((Throwable t, Method method, List<JsonNode> arguments) -> {
            log.error("Exception in json rpc invoke", t);

            int code = ErrorResolver.JsonError.ERROR_NOT_HANDLED.code;
            if (t instanceof BrokerException) {
                code = ((BrokerException) t).getCode();
            }
            return new ErrorResolver.JsonError(code, t.getMessage(), null);
        });

        return exporter;
    }

    //IProducer
    @Bean
    public static IProducer iProducer() {
        try {
            IProducer iProducer = IProducer.build();
            iProducer.startProducer();
            return iProducer;
        } catch (BrokerException e) {
            log.error("start producer error");
            exit();
        }
        return null;
    }

    //IConsumer
    @Bean
    public static IConsumer iConsumer() {

        try {
            IConsumer iConsumer = IConsumer.build();
            iConsumer.startConsumer();
            return iConsumer;
        } catch (BrokerException e) {
            log.error("start consumer error");
            exit();
        }
        return null;
    }

    //http filter
    @Bean
    public static HttpInterceptorConfig interceptorConfig() {
        return new HttpInterceptorConfig();
    }

    //redis
    @Bean(name = "springRedisTemplate")
    @ConditionalOnProperty(prefix = "spring.redis", name = {"host", "port"})
    public static RedisTemplate<String, List<WeEvent>> redisTemplate(LettuceConnectionFactory redisConnectionFactory) {
        try {
            // test Redis connection
            redisConnectionFactory.validateConnection();
        } catch (Exception e) {
            log.error("Unable to connect to Redis, ", e);
            exit();
        }

        RedisTemplate<String, List<WeEvent>> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        redisTemplate.afterPropertiesSet();

        return redisTemplate;
    }

    //ha
    @Bean
    public static MasterJob getMasterJob() {
        return new MasterJob();
    }

    // daemon thread pool
    @Bean
    public static Executor taskExecutor() {
        ThreadPoolTaskExecutor pool = new ThreadPoolTaskExecutor();
        pool.setThreadNamePrefix("weevent-executor-");
        // run in thread immediately, no blocking queue
        pool.setQueueCapacity(0);
        pool.setDaemon(true);
        pool.initialize();

        log.info("init daemon thread pool");
        return pool;
    }
}
