package weevent.robust.config;

import com.webank.weevent.sdk.IWeEventClient;

import com.googlecode.jsonrpc4j.spring.AutoJsonRpcServiceImplExporter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import weevent.RobustApplication;

@Configuration
@IntegrationComponentScan
@Slf4j
public class RobustConfiguration {

    @Value("${weevent.broker.url:(127.0.0.1:7090)}")
    private String url;

    @Value("${mqtt.broker.url:(127.0.0.1:7091)}")
    private String hostUrl;

    @Value("${mqtt.broker.qos:1}")
    private int qos;

    //connect timeout
    @Value("${mqtt.broker.timeout:5000}")
    private long completionTimeout;

    private final static String HTTP_HEADER = "http://";

    final String mqttClientId = "mqttId";

    //jsonrpc4j exporter
    @Bean
    public static AutoJsonRpcServiceImplExporter autoJsonRpcServiceImplExporter() {
        //in here you can provide custom HTTP status code providers etc. eg:
        //exporter.setHttpStatusCodeProvider();
        //exporter.setErrorResolver();
        return new AutoJsonRpcServiceImplExporter();
    }

    @Bean
    public IWeEventClient getBrokerRpc() throws Exception {
        String jsonurl = HTTP_HEADER + url + "/weevent";
        return IWeEventClient.build(jsonurl);
    }

    @Bean
    public WebSocketStompClient getWebSocketStompClient() {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.initialize();

        // standard web socket transport
        WebSocketClient webSocketClient = new StandardWebSocketClient();
        WebSocketStompClient stompClient = new WebSocketStompClient(webSocketClient);

        // no difference in the following
        // MappingJackson2MessageConverter
        stompClient.setMessageConverter(new StringMessageConverter());
        // for heartbeats
        stompClient.setTaskScheduler(taskScheduler);
        return stompClient;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public MqttConnectOptions getMqttConnectOptions() {
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setServerURIs(new String[]{hostUrl});
        //mqttConnectOptions.setKeepAliveInterval(2);
        mqttConnectOptions.setCleanSession(true);
        return mqttConnectOptions;
    }

    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        factory.setConnectionOptions(getMqttConnectOptions());
        return factory;
    }

    @Bean
    @ServiceActivator(inputChannel = "mqttChannel")
    public MessageHandler mqtt() {
        MqttPahoMessageHandler messageHandler = new MqttPahoMessageHandler(mqttClientId, mqttClientFactory());
        messageHandler.setAsync(true);
        messageHandler.setDefaultTopic("com.weevent.mqtt");
        messageHandler.setDefaultQos(qos);
        return messageHandler;
    }

    @Bean
    public MessageChannel mqttChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel mqttInputChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageProducer mqttInbound() {
        /* MqttPahoMessageDrivenChannelAdapter will connect and subscribe even when topic list is empty
           a trick to avoid "Error connecting or subscribing to []"
         */
        MqttPahoMessageDrivenChannelAdapter adapter = new MqttPahoMessageDrivenChannelAdapter(mqttClientId, mqttClientFactory(), "com.weevent.mqtt");
        log.info("MqttPahoMessageDrivenChannelAdapter bean instrument, url: {}", hostUrl);
        // client id can not duplicate
        log.info("mqtt client id: {}", mqttClientId);
        adapter.setCompletionTimeout(completionTimeout);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(qos);
        adapter.setOutputChannel((MessageChannel) RobustApplication.applicationContext.getBean("mqttInputChannel"));
        return adapter;
    }
}


