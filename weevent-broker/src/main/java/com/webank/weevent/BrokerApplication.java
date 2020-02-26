package com.webank.weevent;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.Executor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webank.weevent.broker.config.FiscoConfig;
import com.webank.weevent.broker.config.WeEventConfig;
import com.webank.weevent.broker.fabric.FabricBroker4Consumer;
import com.webank.weevent.broker.fabric.FabricBroker4Producer;
import com.webank.weevent.broker.fabric.config.FabricConfig;
import com.webank.weevent.broker.fabric.sdk.FabricDelegate;
import com.webank.weevent.broker.fisco.FiscoBcosBroker4Consumer;
import com.webank.weevent.broker.fisco.FiscoBcosBroker4Producer;
import com.webank.weevent.broker.fisco.file.FileTransportService;
import com.webank.weevent.broker.fisco.file.ZKChunksMeta;
import com.webank.weevent.broker.fisco.web3sdk.FiscoBcosDelegate;
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
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
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
    private String ipWhiteList;

    HttpInterceptor(String ipWhiteList) {
        this.ipWhiteList = ipWhiteList;
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
        if (StringUtils.isBlank(this.ipWhiteList)) {
            return true;
        }

        String ip = getIpAddress(httpServletRequest);
        log.debug("ip white list:{} client ip:{}", this.ipWhiteList, ip);
        if (ip.contains("0:0:0:0") || ip.contains("127.0.0.1") || ip.contains("localhost")) {
            return true;
        }
        if (!this.ipWhiteList.contains(ip)) {
            log.error("forbid, client ip not in white list, {} -> {}", ip, this.ipWhiteList);
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
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        log.info("client ip white list: {}", BrokerApplication.weEventConfig.getIpWhiteTable());

        HttpInterceptor httpInterceptor = new HttpInterceptor(BrokerApplication.weEventConfig.getIpWhiteTable());
        registry.addInterceptor(httpInterceptor).addPathPatterns("/**");
    }
}


/**
 * Main entrance for broker application.
 *
 * @author matthewliu
 * @since 2018/11/21
 */
@Slf4j
@EnableDiscoveryClient
@SpringBootApplication
public class BrokerApplication {
    public static ApplicationContext applicationContext;
    public static WeEventConfig weEventConfig;

    public static void main(String[] args) {
        /* Forbid banner.
        SpringApplicationBuilder builder = new SpringApplicationBuilder(BrokerApplication.class);
        builder.bannerMode(Banner.Mode.OFF).run(args);
        */
        SpringApplication app = new SpringApplication(BrokerApplication.class);
        app.addListeners(new ApplicationPidFileWriter());
        app.run();

        log.info("read from weevent.properties, {}", weEventConfig);
    }

    @Autowired
    public void setContext(ApplicationContext applicationContext) {
        BrokerApplication.applicationContext = applicationContext;
    }

    @Autowired
    public void setWeEventConfig(WeEventConfig weEventConfig) {
        BrokerApplication.weEventConfig = weEventConfig;
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

    // support CORS
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

    // FiscoBcosDelegate
    @Bean
    @ConditionalOnProperty(prefix = "broker.blockchain", name = "type", havingValue = "fisco")
    public static FiscoBcosDelegate fiscoBcosDelegate(FiscoConfig fiscoConfig) throws BrokerException {
        FiscoBcosDelegate fiscoBcosDelegate = new FiscoBcosDelegate();
        fiscoBcosDelegate.initProxy(fiscoConfig);

        return fiscoBcosDelegate;
    }

    // FabricDelegate
    @Bean
    @ConditionalOnProperty(prefix = "broker.blockchain", name = "type", havingValue = "fabric")
    public static FabricDelegate fabricDelegate(FabricConfig fabricConfig) throws BrokerException {
        FabricDelegate fabricDelegate = new FabricDelegate();
        fabricDelegate.initProxy(fabricConfig);

        return fabricDelegate;
    }

    // FISCO-BCOS IConsumer
    @Bean(name = "iConsumer")
    @ConditionalOnBean(FiscoBcosDelegate.class)
    public static IConsumer fiscoIConsumer(FiscoBcosDelegate fiscoBcosDelegate) throws BrokerException {
        FiscoBcosBroker4Consumer fiscoBcosBroker4Consumer = new FiscoBcosBroker4Consumer(fiscoBcosDelegate);
        boolean startConsumer = fiscoBcosBroker4Consumer.startConsumer();
        return fiscoBcosBroker4Consumer;
    }

    // Fabric IConsumer
    @Bean(name = "iConsumer")
    @ConditionalOnBean(FiscoBcosDelegate.class)
    public static IConsumer fabricIConsumer(FabricDelegate fabricDelegate) throws BrokerException {
        FabricBroker4Consumer fabricBroker4Consumer = new FabricBroker4Consumer(fabricDelegate);
        fabricBroker4Consumer.startConsumer();
        return fabricBroker4Consumer;
    }

    // FISCO-BCOS IProducer
    @Bean(name = "iProducer")
    @ConditionalOnBean(FiscoBcosDelegate.class)
    public static IProducer fiscoIProducer(FiscoBcosDelegate fiscoBcosDelegate) {
        FiscoBcosBroker4Producer fiscoBcosBroker4Producer = new FiscoBcosBroker4Producer(fiscoBcosDelegate);
        fiscoBcosBroker4Producer.startProducer();
        return fiscoBcosBroker4Producer;
    }

    // Fabric IProducer
    @Bean(name = "iProducer")
    @ConditionalOnBean(FabricDelegate.class)
    public static IProducer fabricIProducer(FabricDelegate fabricDelegate) {
        FabricBroker4Producer fabricBroker4Producer = new FabricBroker4Producer(fabricDelegate);
        fabricBroker4Producer.startProducer();
        return fabricBroker4Producer;
    }

    // FileChunksMeta in Zookeeper
    @Bean
    @ConditionalOnProperty(prefix = "spring.cloud.zookeeper", name = "enabled", havingValue = "true", matchIfMissing = true)
    public static ZKChunksMeta getZKChunksMeta(Environment environment) throws BrokerException {
        String connectString = "127.0.0.1:2181";
        final String key = "spring.cloud.zookeeper.connect-string";
        if (environment.containsProperty(key)) {
            connectString = environment.getProperty(key);
        }

        return new ZKChunksMeta("/WeEvent/files", connectString);
    }

    @Bean
    @ConditionalOnBean(ZKChunksMeta.class)
    public static FileTransportService getFileService(FiscoConfig fiscoConfig,
                                                      IProducer iProducer,
                                                      ZKChunksMeta zkChunksMeta,
                                                      Environment environment,
                                                      WeEventConfig weEventConfig) throws BrokerException {
        return new FileTransportService(fiscoConfig,
                iProducer,
                zkChunksMeta,
                environment.getProperty("spring.cloud.zookeeper.discovery.instance-id"),
                weEventConfig.getFilePath(),
                weEventConfig.getFileChunkSize());
    }

    // http filter
    @Bean
    public static HttpInterceptorConfig interceptorConfig() {
        return new HttpInterceptorConfig();
    }

    // redis
    @Bean(name = "springRedisTemplate")
    @ConditionalOnProperty(prefix = "spring.redis", name = {"host", "port"})
    public static RedisTemplate<String, List<WeEvent>> redisTemplate(LettuceConnectionFactory redisConnectionFactory) throws BrokerException {
        try {
            // test Redis connection
            redisConnectionFactory.validateConnection();
        } catch (Exception e) {
            log.error("Unable to connect to Redis. ", e);
            throw new BrokerException("Unable to connect to Redis");
        }

        RedisTemplate<String, List<WeEvent>> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        redisTemplate.afterPropertiesSet();

        return redisTemplate;
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
