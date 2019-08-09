package weevent.robust.service;

import com.alibaba.fastjson.JSONObject;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.IWeEventClient;
import com.webank.weevent.sdk.SendResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.stomp.*;
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
import weevent.robust.service.interfaces.MqttGateway;
import weevent.robust.util.FileUtil;
import weevent.robust.util.StringUtil;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ExecutionException;


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
public class ScheduledService {


    @Value("${weevent.broker.url}")
    private String url;

    @Value("${statistic.file.path}")
    private String statisticFilePath;

    @Value("${subscripId.file.path}")
    private String subscripIdPath;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private IWeEventClient weEventClient;

    @Autowired
    private WebSocketStompClient stompClient;

    @Autowired
    private MqttGateway mqttGateway;

    private static StompSession stompSession;

    private final static String format = "yyyy-MM-dd HH";

    public static Map<String, Integer> statisticMap = new HashMap<>();
    public static Map<String, Integer> restfulSendMap = new HashMap<>();
    public static Map<String, Integer> jsonrpcSendMap = new HashMap<>();
    public static Map<String, Integer> mqttSendMap = new HashMap<>();
    public static Map<String, Integer> stompSendMap = new HashMap<>();
    public static Map<String, Integer> brokerSendMap = new HashMap<>();

    @PostConstruct
    public void init() throws IOException, InterruptedException, ExecutionException, BrokerException {
        File subIdFile = new File(subscripIdPath);
        String subText = FileUtil.readTxt(subIdFile);
        List<String> subIds = getSubIds(subText);
        StringBuffer urlBuffer = StringUtil.getIntegralUrl(StringUtil.HTTP_HEADER, url, "/weevent/rest/unSubscribe?subscriptionId={subscriptionId}");
        for (String subId : subIds) {
            // cancel the original subscription
            restTemplate.getForEntity(urlBuffer.toString(), String.class, subId);
        }
        FileUtil.writeStringToFile(subIdFile.getAbsolutePath(), "", false);
        urlBuffer = StringUtil.getIntegralUrl("ws://", url, "/weevent/stomp");
        ListenableFuture<StompSession> listenableFuture = stompClient.connect(urlBuffer.toString(), getStompSessionHandlerAdapter());

        this.stompSession = listenableFuture.get();

        // stomp subscribe
        stompSession.setAutoReceipt(true);
        stompSession.subscribe("com.weevent.stomp", getStompFrameHander());
    }


    @Async
    @Scheduled(cron = "5 0/1 * * * *")
    public void scheduled() throws BrokerException {
        // use the rest request to post a message
        weEventClient.open("com.weevent.rest");
        StringBuffer urlBuffer = StringUtil.getIntegralUrl(StringUtil.HTTP_HEADER, url, "/weevent/rest/publish?topic={topic}&content={content}");
        ResponseEntity<String> rsp = restTemplate.getForEntity(
                urlBuffer.toString(),
                String.class,
                "com.weevent.rest",
                "hello weevent rest");
        log.info("restful send message:" + rsp.getBody());
        if (rsp.getStatusCodeValue() == 200) {
            countTimes(restfulSendMap, StringUtil.getFormatTime(format, new Date()));
        }
    }

    @Async
    @Scheduled(cron = "10 0/1 * * * *")
    public void scheduled2() throws BrokerException {
        // use jsonRpc publish topic
        String topic = "com.weevent.jsonrpc";
        weEventClient.open(topic);
        SendResult publish = weEventClient.publish(topic, "Hello World !".getBytes());
        log.info("jsonrpc send message:" + publish.getEventId());
        if (publish.getStatus() == SendResult.SendResultStatus.SUCCESS) {
            countTimes(jsonrpcSendMap, StringUtil.getFormatTime(format, new Date()));
        }
    }

    @Async
    @Scheduled(cron = "15 0/1 * * * *")
    public void scheduled3() {
        try {
            stompSession.send("com.weevent.stomp", "hello world from websocket");
            log.info("stomp send msg!");
            countTimes(stompSendMap, StringUtil.getFormatTime(format, new Date()));
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    @Async
    @Scheduled(cron = "20 0/1 * * * *")
    public void scheduled4() throws BrokerException {
        // Mqtt sends a message to weevent broker
        mqttGateway.sendToMqtt("hello mqtt", "com.weevent.mqtt");
        log.info("mqtt send msg to broker");
        countTimes(mqttSendMap, StringUtil.getFormatTime(format, new Date()));
    }

    @Async
    @Scheduled(cron = "25 0/1 * * * *")
    public void scheduled5() throws BrokerException {
        //use rest request to send a message from weevent broker to mqtt
        StringBuffer urlBuffer = StringUtil.getIntegralUrl(StringUtil.HTTP_HEADER, url, "/weevent/rest/publish?topic={topic}&content={content}");
        ResponseEntity<String> rsp = restTemplate.getForEntity(
                urlBuffer.toString(),
                String.class,
                "com.weevent.mqtt",
                "hello weevent");
        log.info("send msg to mqtt:" + rsp.getBody());
        if (rsp.getStatusCodeValue() == 200) {
            countTimes(brokerSendMap, StringUtil.getFormatTime(format, new Date()));
        }
    }

    @Async
    @Scheduled(cron = "40 10 * * * *")
    public void writeStatisticIntoFile() {
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.HOUR, -1);
        Date lastHour = calendar.getTime();
        String time = StringUtil.getFormatTime(format, lastHour);

        FileUtil.writeStringToFile(statisticFilePath, "Time is " + StringUtil.getFormatTime(format, date) + ":00:00\n", true);
        log.info(statisticFilePath, "Time is " + StringUtil.getFormatTime(format, date) + ":00:00\n");

        FileUtil.writeStringToFile(statisticFilePath,
                "last hour restful send: " + restfulSendMap.get(time) + ", receive:" + restfulSendMap.get(time) + " events\n", true);
        log.info("last hour restful send: " + restfulSendMap.get(time) + ", receive:" + restfulSendMap.get(time) + " events\n");

        FileUtil.writeStringToFile(statisticFilePath,
                "last hour stomp send: " + stompSendMap.get(time) + ", receive:" + stompSendMap.get(time) + " events\n", true);
        log.info("last hour stomp send: " + stompSendMap.get(time) + ", receive:" + stompSendMap.get(time) + " events\n");

        FileUtil.writeStringToFile(statisticFilePath,
                "last hour stomp send: " + mqttSendMap.get(time) + ", receive:" + mqttSendMap.get(time) + " events\n", true);
        log.info("last hour stomp send: " + mqttSendMap.get(time) + " receive:" + mqttSendMap.get(time) + " events\n");

        FileUtil.writeStringToFile(statisticFilePath,
                "last hour stomp send: " + jsonrpcSendMap.get(time) + ", receive:" + jsonrpcSendMap.get(time) + " events\n", true);
        log.info("last hour stomp send: " + jsonrpcSendMap.get(time) + " receive:" + jsonrpcSendMap.get(time) + " events\n");

        FileUtil.writeStringToFile(statisticFilePath,
                "last hour stomp send: " + brokerSendMap.get(time) + ", receive:" + brokerSendMap.get(time) + " events\n", true);
        log.info("last hour stomp send: " + brokerSendMap.get(time) + " receive:" + brokerSendMap.get(time) + " events\n");
        //remove last hour statistic key - value
        statisticMap.remove(time);
        restfulSendMap.remove(time);
        stompSendMap.remove(time);
        mqttSendMap.remove(time);
        jsonrpcSendMap.remove(time);
        brokerSendMap.remove(time);


    }

    private StompFrameHandler getStompFrameHander() {

        return new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return String.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                log.info("subscribe handleFrame, header: {} payload: {}", headers, payload);
                //  the current system time
                String time = StringUtil.getFormatTime(format, new Date());
                countTimes(statisticMap, time);
            }
        };
    }

    private StompSessionHandlerAdapter getStompSessionHandlerAdapter() {
        return new StompSessionHandlerAdapter() {
            @Override
            public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                log.info("connection open, {}", session.getSessionId());
            }

            @Override
            public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
                log.info("connection exception, {} {}", session.getSessionId(), command);
                log.error("exception, {}", exception);
            }

            @Override
            public void handleTransportError(StompSession session, Throwable exception) {
                if (exception instanceof ConnectionLostException && !session.isConnected()) {
                    try {
                        Thread.sleep(5000);
                        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
                        taskScheduler.initialize();
                        //new connect--start
                        WebSocketClient webSocketClient = new StandardWebSocketClient();
                        WebSocketStompClient stompClient = new WebSocketStompClient(webSocketClient);
                        stompClient.setMessageConverter(new StringMessageConverter());
                        stompClient.setTaskScheduler(taskScheduler); // for heartbeats
                        ListenableFuture<StompSession> f = stompClient.connect("ws://" + url + "/weevent/stomp", this);
                        stompSession = f.get();
                        // stomp订阅
                        stompSession.setAutoReceipt(true);
                        stompSession.subscribe("com.weevent.stomp", getStompFrameHander());
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
     * @param subIdStrs
     * @return List<String>
     */
    private static List<String> getSubIds(String subIdStrs) {
        JSONObject jsonObject = JSONObject.parseObject(subIdStrs);
        List<String> subIds = new ArrayList<>();
        if (null != jsonObject && jsonObject.containsKey(InitialService.SUBSCRIBE_ID)) {
            subIds = (List) jsonObject.get(InitialService.SUBSCRIBE_ID);
        }
        return subIds;
    }

    /**
     * count times
     *
     * @param integerMap
     * @param timeKey
     */
    private void countTimes(Map<String, Integer> integerMap, String timeKey) {
        if (integerMap.containsKey(timeKey)) {
            integerMap.put(timeKey, (integerMap.get(timeKey) + 1));
        } else {
            integerMap.put(timeKey, 1);
        }
    }


}
