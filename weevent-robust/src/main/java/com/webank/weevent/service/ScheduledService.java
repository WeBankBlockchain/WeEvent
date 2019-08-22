package com.webank.weevent.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import com.webank.weevent.RobustApplication;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.IWeEventClient;
import com.webank.weevent.sdk.SendResult;
import com.webank.weevent.sdk.jsonrpc.IBrokerRpc;
import com.webank.weevent.service.interfaces.MqttGateway;

import com.alibaba.fastjson.JSONObject;
import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import com.googlecode.jsonrpc4j.ProxyUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.stomp.ConnectionLostException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;


/**
 * This is a timing tool class that monitors whether the borker service is up and running.
 *
 * @author junyanliu
 * @author puremilkfan
 */

@Slf4j
@Component
@EnableScheduling
@EnableAsync
public class ScheduledService implements AutoCloseable {


    @Value("${weevent.broker.url:(127.0.0.1:7090)}")
    private String url;

    @Value("${statistic.file.path:(./logs/countTimes.text)}")
    private String statisticFilePath;

    @Value("${subscripId.file.path:(./logs/subscriptionId.json)}")
    private String subscripIdPath;


    private final static String format = "yyyy-MM-dd HH";

    private final static String SUBSCRIBE_ID = "subscribeId";

    private final static String HTTP_HEADER = "http://";

    private final static String REST_TOPIC = "com.weevent.rest";

    private final static String JSON_RPC_TOPIC = "com.weevent.jsonrpc";

    private final static String STOMP_TOPIC = "com.weevent.stomp";

    private final static String MQTT_TOPIC = "com.weevent.mqtt";

    private static Map<String, String> topicSubscribeMap = new HashMap<>();

    private static Map<String, Integer> restfulSendMap = new HashMap<>();
    private static Map<String, Integer> jsonrpcSendMap = new HashMap<>();
    private static Map<String, Integer> mqttSendMap = new HashMap<>();
    private static Map<String, Integer> stompSendMap = new HashMap<>();

    private StompSession stompSession;

    private MqttGateway mqttGateway;

    private RestTemplate restTemplate;

    private IWeEventClient weEventClient;

    private WebSocketStompClient socketStompClient;

    @Autowired
    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Autowired
    public void setWeEventClient(IWeEventClient weEventClient) {
        this.weEventClient = weEventClient;
    }

    @Autowired
    public void setSocketStompClient(WebSocketStompClient socketStompClient) {
        this.socketStompClient = socketStompClient;
    }

    public ScheduledService() {
        this.mqttGateway = RobustApplication.applicationContext.getBean(MqttGateway.class);
    }

    @Async
    @Scheduled(cron = "0/10 * * * * ?")
    public synchronized void scheduled() throws BrokerException, IOException, InterruptedException, ExecutionException {
        File subIdFile = new File(subscripIdPath);
        String subText = this.readTxt(subIdFile);
        topicSubscribeMap = getSubIdMap(subText);
        //stomp subscribe
        this.stompSubscribe();
        //restful subscribe
        topicSubscribeMap.put(REST_TOPIC, this.restfulSubscribe(topicSubscribeMap.get(REST_TOPIC)));
        //jsonRpc subscribe
        topicSubscribeMap.put(JSON_RPC_TOPIC, this.jsonRpcSubscribe(topicSubscribeMap.get(JSON_RPC_TOPIC)));

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(SUBSCRIBE_ID, topicSubscribeMap);
        this.writeStringToFile(subscripIdPath, jsonObject.toJSONString(), false);
        // use the rest request to post a message
        this.restfulPublic();
        // use jsonRpc publish topic
        this.jsonrpcPublish();
        // use stomp publish topic
        this.stompPublish();
        // use mqtt publish topic
        this.mqttPublish();
    }

    private String restfulSubscribe(String oldSubscribe) throws RestClientException {
        String callUrl = HTTP_HEADER + url + "/weevent/rest/subscribe?topic={topic}&groupId={groupId}&subscriptionId={subscriptionId}&url={url}";
        String callBackUrl = HTTP_HEADER + url + "/weevent/mock/rest/onEvent";
        ResponseEntity<String> rsp = this.restTemplate.getForEntity(callUrl, String.class, REST_TOPIC, "1", oldSubscribe, callBackUrl);
        String subId = rsp.getBody();
        log.info("rest subId: " + subId);
        return subId;
    }

    private String jsonRpcSubscribe(String oldSubscribe) throws BrokerException, MalformedURLException {
        String callUrl = HTTP_HEADER + url + "/weevent/jsonrpc";
        String callBackUrl = HTTP_HEADER + url + "/weevent/mock/jsonrpc/onEvent";
        URL remote = new URL(callUrl);
        // init jsonrpc client
        JsonRpcHttpClient client = new JsonRpcHttpClient(remote);
        // init IBrokerRpc object
        IBrokerRpc rpc = ProxyUtil.createClientProxy(client.getClass().getClassLoader(), IBrokerRpc.class, client);
        // open topic
        rpc.open(JSON_RPC_TOPIC, "1");
        // publish event
        String subscribeId = rpc.subscribe(JSON_RPC_TOPIC, "1", oldSubscribe, callBackUrl);
        log.info("jsonrpc subId: " + subscribeId);
        return subscribeId;
    }

    private void stompSubscribe() throws InterruptedException, ExecutionException {
        String callUrl = "ws://" + url + "/weevent/stomp";
        StompSessionHandlerAdapter handlerAdapter = this.getStompSessionHandlerAdapter();
        ListenableFuture<StompSession> connect = this.socketStompClient.connect(callUrl, handlerAdapter);
        StompHeaders header = new StompHeaders();
        header.setDestination(STOMP_TOPIC);
        header.set("groupId", "1");
        header.set("weevent-subscriptionId", topicSubscribeMap.get(STOMP_TOPIC));
        // extension params
        header.set("weevent-format", "json");
        StompSession session = connect.get();
        StompSession.Subscription subscribe = session.subscribe(header, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                List<String> subscriptionIds = headers.get("subscription-id");
                if (subscriptionIds != null && subscriptionIds.size() > 0) {
                    topicSubscribeMap.put(STOMP_TOPIC, subscriptionIds.get(0));
                    log.info("stomp subId: " + subscriptionIds.get(0));
                }
                return this.getClass();
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                log.info(payload.toString());
            }
        });
        log.info(subscribe.getSubscriptionId());
    }


    private void restfulPublic() throws BrokerException {
        // use the rest request to post a message
        boolean result = this.weEventClient.open(REST_TOPIC);
        log.info("rest open result is " + result);

        String callUrl = HTTP_HEADER + this.url + "/weevent/rest/publish?topic={topic}&content={content}";
        ResponseEntity<String> rsp = this.restTemplate.getForEntity(
                callUrl,
                String.class,
                REST_TOPIC,
                "hello weevent restful");
        log.info("restful send message:" + rsp.getBody());
        if (rsp.getStatusCodeValue() == 200) {
            countTimes(restfulSendMap, this.getFormatTime(new Date()));
        }
    }

    private void jsonrpcPublish() throws BrokerException {
        // use jsonRpc publish topic
        this.weEventClient.open(JSON_RPC_TOPIC);
        SendResult publish = this.weEventClient.publish(JSON_RPC_TOPIC, "Hello JsonRpc !".getBytes());
        log.info("JsonRpc send message:" + publish.getEventId());
        if (publish.getStatus() == SendResult.SendResultStatus.SUCCESS) {
            countTimes(jsonrpcSendMap, this.getFormatTime(new Date()));
        }
    }

    private void stompPublish() throws BrokerException {
        this.weEventClient.open(STOMP_TOPIC);
        this.stompSession.send(STOMP_TOPIC, "hello world from websocket");
        log.info("stomp send msg!");
        countTimes(stompSendMap, this.getFormatTime(new Date()));
    }

    private void mqttPublish() throws BrokerException {
        // Mqtt sends a message to weevent broker
        String data = "hello mqtt";
        this.weEventClient.open(MQTT_TOPIC);
        mqttGateway.sendToMqtt(data, MQTT_TOPIC);
        log.info("mqtt send msg to broker");
        countTimes(mqttSendMap, this.getFormatTime(new Date()));
    }


    @Async
    @Scheduled(cron = "0 0 0/1 * * *")
    public void writeStatisticIntoFile() {
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.HOUR, -1);
        Date lastHour = calendar.getTime();
        String time = this.getFormatTime(lastHour);

        this.writeStringToFile(statisticFilePath, "Time is " + time + ":00:00\n", true);
        log.info(statisticFilePath, "Time is " + this.getFormatTime(date) + ":00:00\n");

        this.writeStringToFile(statisticFilePath,
                "last hour restful send: " + restfulSendMap.get(time) + ", receive:" + restfulSendMap.get(time) + " events\n", true);
        log.info("last hour restful send: " + restfulSendMap.get(time) + ", receive:" + restfulSendMap.get(time) + " events\n");

        this.writeStringToFile(statisticFilePath,
                "last hour stomp send: " + stompSendMap.get(time) + ", receive:" + stompSendMap.get(time) + " events\n", true);
        log.info("last hour stomp send: " + stompSendMap.get(time) + ", receive:" + stompSendMap.get(time) + " events\n");

        this.writeStringToFile(statisticFilePath,
                "last hour mqtt send: " + mqttSendMap.get(time) + ", receive:" + mqttSendMap.get(time) + " events\n", true);
        log.info("last hour mqtt send: " + mqttSendMap.get(time) + " receive:" + mqttSendMap.get(time) + " events\n");

        this.writeStringToFile(statisticFilePath,
                "last hour json rpc send: " + jsonrpcSendMap.get(time) + ", receive:" + jsonrpcSendMap.get(time) + " events\n", true);
        log.info("last hour json rpc send: " + jsonrpcSendMap.get(time) + " receive:" + jsonrpcSendMap.get(time) + " events\n");

        //remove last hour statistic key - value
        restfulSendMap.remove(time);
        stompSendMap.remove(time);
        mqttSendMap.remove(time);
        jsonrpcSendMap.remove(time);


    }

    private StompSessionHandlerAdapter getStompSessionHandlerAdapter() {
        return new StompSessionHandlerAdapter() {
            @Override
            public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                setStompSession(session);
                log.info("connection open, {}", session.getSessionId());
                session.setAutoReceipt(true);
            }

            @Override
            public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
                log.info("connection exception, {} {}", session.getSessionId(), command);
            }


            @Override
            public void handleTransportError(StompSession session, Throwable exception) {
                if (exception instanceof ConnectionLostException && !session.isConnected()) {
                    try {
                        Thread.sleep(2000);
                        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
                        taskScheduler.initialize();
                        //new connect--start
                        WebSocketClient webSocketClient = new StandardWebSocketClient();
                        WebSocketStompClient stompClient = new WebSocketStompClient(webSocketClient);
                        stompClient.setMessageConverter(new StringMessageConverter());
                        stompClient.setTaskScheduler(taskScheduler);
                        ListenableFuture<StompSession> future = stompClient.connect("ws://" + url + "/weevent/stomp", this);
                        stompSession = future.get();
                        // stomp subscribe
                        stompSession.setAutoReceipt(true);
                    } catch (InterruptedException | ExecutionException e) {
                        log.error(e.getMessage());
                    }
                }
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                log.info("session handleFrame, header: {} payload: {}", headers, payload);
            }
        };
    }

    /**
     * Convert json to List
     *
     * @param subIdString json string
     * @return List<String>
     */
    @SuppressWarnings("unchecked")
    private static Map<String, String> getSubIdMap(String subIdString) {
        JSONObject jsonObject = JSONObject.parseObject(subIdString);
        Map<String, String> map = new HashMap<>();
        if (null != jsonObject && jsonObject.containsKey(SUBSCRIBE_ID)) {
            Object object = jsonObject.get(SUBSCRIBE_ID);
            if (object != null) {
                map = (Map<String, String>) object;
            }
        }
        return map;
    }

    private void countTimes(Map<String, Integer> integerMap, String timeKey) {
        if (integerMap.containsKey(timeKey)) {
            integerMap.put(timeKey, (integerMap.get(timeKey) + 1));
        } else {
            integerMap.put(timeKey, 1);
        }
    }


    /**
     * get formatted time string
     *
     * @param date the time
     * @return string
     */
    private String getFormatTime(Date date) {
        return DateFormatUtils.format(date, format);
    }

    /**
     * Read the file to get the file content
     *
     * @param file the file
     * @return String
     */
    private String readTxt(File file) throws IOException {
        if (!file.isFile() || !file.exists()) {
            boolean newFile = file.createNewFile();
            if (newFile) {
                log.info("createFile success");
            }
        }
        InputStream inputStream = new FileInputStream(file);
        return IOUtils.toString(inputStream, "utf8");
    }

    /**
     * @param filePath is the file path,
     * @param content content needs to be written
     * @param flag is true for append, false for overwrite
     */
    private void writeStringToFile(String filePath, String content, boolean flag) {
        File file = new File(filePath);
        if (!file.exists()) {
            try {
                boolean newFile = file.createNewFile();
                if (newFile) {
                    log.info("createFile success");
                }
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }
        try (OutputStream os = new FileOutputStream(file, flag)) {
            byte[] b = content.getBytes();
            os.write(b);
            os.flush();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    private void setStompSession(StompSession session) {
        this.stompSession = session;
    }

    @Override
    public void close() {
        log.info("resource is close");
    }

}


