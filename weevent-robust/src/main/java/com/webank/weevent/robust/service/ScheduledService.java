package com.webank.weevent.robust.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

import javax.annotation.PostConstruct;

import com.webank.weevent.robust.RobustApplication;
import com.webank.weevent.robust.service.interfaces.MqttGateway;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.IWeEventClient;
import com.webank.weevent.sdk.SendResult;
import com.webank.weevent.sdk.jsonrpc.IBrokerRpc;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
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

    @Value("${mqtt.broker.qos:1}")
    private int qos;

    @Value("${weevent.broker.url:(127.0.0.1:7090)}")
    private String url;

    @Value("${statistic.file.path:(./logs/countTimes.txt)}")
    private String statisticFilePath;

    private final static String FORMAT = "yyyy-MM-dd HH";

    private final static String HTTP_HEADER = "http://";

    private final static String REST_TOPIC = "com.weevent.rest";

    private final static String JSON_RPC_TOPIC = "com.weevent.jsonrpc";

    private final static String STOMP_TOPIC = "com.weevent.stomp";

    private final static String MQTT_TOPIC = "com.weevent.mqtt";


    private final static String EVENT_ID = "eventId";

    private static Map<String, String> topicSubscribeMap = new ConcurrentHashMap<>();

    private final static Map<String, Integer> restfulSendMap = new ConcurrentHashMap<>();
    private final static Map<String, Integer> jsonrpcSendMap = new ConcurrentHashMap<>();
    private final static Map<String, Integer> mqttSendMap = new ConcurrentHashMap<>();
    private final static Map<String, Integer> stompSendMap = new ConcurrentHashMap<>();

    private final static Map<String, Integer> mqttReceiveMap = new ConcurrentHashMap<>();
    private final static Map<String, Integer> stompReceiveMap = new ConcurrentHashMap<>();

    private StompSession stompSession;

    private RestTemplate restTemplate;

    private IWeEventClient weEventClient;

    private WebSocketStompClient socketStompClient;

    private IBrokerRpc brokerRpc;

    private MqttGateway mqttGateway;

    private Integer countStomp = 1;


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

    @Autowired
    public void setIBrokerRpc(IBrokerRpc brokerRpc) {
        this.brokerRpc = brokerRpc;
    }

    public ScheduledService() {
        this.mqttGateway = RobustApplication.applicationContext.getBean(MqttGateway.class);
    }

    public static Map<String, Integer> getMqttReceiveMap() {
        return mqttReceiveMap;
    }

    @PostConstruct
    public void init() throws BrokerException {
        this.writeStringToFile(statisticFilePath, "", false);
        boolean result = weEventClient.open(REST_TOPIC);
        log.info("rest topic  open result is {}", result);
        result = weEventClient.open(JSON_RPC_TOPIC);
        log.info("json topic  rpc open result is {}", result);
        result = weEventClient.open(STOMP_TOPIC);
        log.info("stomp topic  open result is {}", result);
        result = weEventClient.open(MQTT_TOPIC);
        log.info(" mqtt topic open result is {}", result);
        //stomp subscribe
        this.stompSubscribe();
    }

    @Async
    @Scheduled(cron = "0/30 * * * * ?")
    public synchronized void scheduled() throws BrokerException {

        // use the rest request to post a message
        this.restfulPublic();
        // use jsonRpc publish topic
        this.jsonRpcPublish();
        // use stomp publish topic
        this.stompPublish();
        // use mqtt publish topic
        this.mqttPublish();
    }

    private void stompSubscribe() {
        String callUrl = "ws://" + url + "/weevent/stomp";
        StompSessionHandlerAdapter handlerAdapter = this.getStompSessionHandlerAdapter();
        this.socketStompClient.connect(callUrl, handlerAdapter);
    }

    private void restfulPublic() {
        // use the rest request to post a message
        String callUrl = HTTP_HEADER + this.url + "/weevent/rest/publish?topic={topic}&content={content}";
        ResponseEntity<String> rsp = this.restTemplate.getForEntity(
                callUrl,
                String.class,
                REST_TOPIC,
                "hello weevent restful");
        log.info("restful send message:{}", rsp.getBody());
        if (rsp.getStatusCodeValue() == 200) {
            countTimes(restfulSendMap, this.getFormatTime(new Date()));
        }
    }

    private void jsonRpcPublish() throws BrokerException {
        // use jsonRpc publish topic
        SendResult publish = brokerRpc.publish(JSON_RPC_TOPIC, "Hello JsonRpc !".getBytes());
        log.info("jsonRpc send message:EventId{}" + publish.getEventId());
        if (publish.getStatus() == SendResult.SendResultStatus.SUCCESS) {
            countTimes(jsonrpcSendMap, this.getFormatTime(new Date()));
        }
    }

    private void stompPublish() {
        StompSession.Receiptable receipt = this.stompSession.send(STOMP_TOPIC, "hello world from websocket");
        String receiptId = receipt == null ? "0" : receipt.getReceiptId();
        log.info("stomp send msg. receiptId{}", receiptId);
        if (receiptId != null && Integer.valueOf(receiptId) > 0) {
            countTimes(stompSendMap, this.getFormatTime(new Date()));
        }

    }

    private void mqttPublish() {
        // Mqtt sends a message to weevent broker
        mqttGateway.sendToMqtt(MQTT_TOPIC, "hello mqtt");
        countTimes(mqttSendMap, this.getFormatTime(new Date()));
        log.info("mqtt send msg");
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
                "last hour restful send: " + restfulSendMap.get(time) + " events\n", true);
        log.info("last hour restful send: " + restfulSendMap.get(time) + " events\n");


        this.writeStringToFile(statisticFilePath,
                "last hour json rpc send: " + jsonrpcSendMap.get(time) + " events\n", true);
        log.info("last hour json rpc send: " + jsonrpcSendMap.get(time) + " events\n");

        this.writeStringToFile(statisticFilePath,
                "last hour stomp send: " + stompSendMap.get(time) + ", receive:" + stompReceiveMap.get(time) + " events\n", true);
        log.info("last hour stomp send: " + stompSendMap.get(time) + ", receive:" + stompReceiveMap.get(time) + " events\n");

        this.writeStringToFile(statisticFilePath,
                "last hour mqtt send: " + mqttSendMap.get(time) + ", receive:" + mqttReceiveMap.get(time) + " events\n", true);
        log.info("last hour mqtt send: " + mqttSendMap.get(time) + " receive:" + mqttReceiveMap.get(time) + " events\n");


        //remove last hour statistic key - value
        restfulSendMap.remove(time);
        stompSendMap.remove(time);
        mqttSendMap.remove(time);
        jsonrpcSendMap.remove(time);
        stompReceiveMap.remove(time);
        mqttReceiveMap.remove(time);

    }

    private StompSessionHandlerAdapter getStompSessionHandlerAdapter() {
        return new StompSessionHandlerAdapter() {
            @Override
            public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                setStompSession(session);
                log.info("connection open, {}", session.getSessionId());
                session.setAutoReceipt(true);
                // extension params
                StompHeaders header = new StompHeaders();
                header.setDestination(STOMP_TOPIC);
                header.set("groupId", "1");
                header.set(EVENT_ID, topicSubscribeMap.get(EVENT_ID));
                header.set("weevent-subscriptionId", topicSubscribeMap.get(STOMP_TOPIC));
                header.set("weevent-format", "json");

                session.subscribe(header, new StompFrameHandler() {
                    @Override
                    public Type getPayloadType(StompHeaders headers) {
                        List<String> subscriptionIds = headers.get("subscription-id");
                        if (subscriptionIds != null && subscriptionIds.size() > 0) {
                            topicSubscribeMap.put(STOMP_TOPIC, subscriptionIds.get(0));
                            log.info("stomp subId: " + subscriptionIds.get(0));
                        }
                        return String.class;
                    }

                    @Override
                    public void handleFrame(StompHeaders headers, Object payload) {
                        //count receive times
                        if (topicSubscribeMap.get(EVENT_ID) != null && topicSubscribeMap.get(STOMP_TOPIC) != null) {
                            countTimes(stompReceiveMap, getFormatTime(new Date()));
                        }
                        ObjectMapper objectMapper = new ObjectMapper();
                        Map map = null;
                        try {
                            map = objectMapper.readValue(payload.toString(), Map.class);
                        } catch (IOException e) {
                            log.error("json conversion failed", e);
                            e.printStackTrace();
                        }
                        if (map.get(EVENT_ID) != null) {
                            String eventId = map.get("eventId").toString();
                            topicSubscribeMap.put(EVENT_ID, eventId);
                            log.info("stomp  eventId: {},countStomp:{}", eventId, countStomp++);
                        }
                    }
                });
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

    public static synchronized void countTimes(Map<String, Integer> integerMap, String timeKey) {
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
        return DateFormatUtils.format(date, FORMAT);
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
