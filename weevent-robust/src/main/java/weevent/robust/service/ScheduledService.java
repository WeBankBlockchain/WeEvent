package weevent.robust.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.annotation.PostConstruct;

import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.IWeEventClient;
import com.webank.weevent.sdk.SendResult;
import com.webank.weevent.sdk.WeEvent;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
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
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import weevent.robust.service.interfaces.MqttGateway;


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
    private MqttPahoMessageHandler mqttPahoMessageHandler;


    @Autowired
    private MqttGateway mqttGateway;

  //  @Autowired
 //   private MqttBridge mqttBridge;

    private  StompSession stompSession;


    @Autowired
    private WebSocketStompClient socketStompClient;

    private final static String format = "yyyy-MM-dd HH";

    public final static String SUBSCRIBE_ID = "subscribeId";

    private final static String HTTP_HEADER = "http://";



    public static Map<String, Integer> statisticMap = new HashMap<>();
    public static Map<String, Integer> restfulSendMap = new HashMap<>();
    public static Map<String, Integer> jsonrpcSendMap = new HashMap<>();
    public static Map<String, Integer> mqttSendMap = new HashMap<>();
    public static Map<String, Integer> stompSendMap = new HashMap<>();

    @PostConstruct
    public void init() throws IOException, BrokerException,InterruptedException, ExecutionException{
        final List<String> subIdList = new ArrayList<>();
        File subIdFile = new File(subscripIdPath);
        String subText = this.readTxt(subIdFile);
        List<String> subIds = getSubIds(subText);
        String callUrl =HTTP_HEADER+url+"/weevent/rest/unSubscribe?subscriptionId={subscriptionId}";
        for (String subId : subIds) {
            // cancel the original subscription
            restTemplate.getForEntity(callUrl, String.class, subId);
        }
        this.writeStringToFile(subIdFile.getAbsolutePath(), "", false);

        // subscribe rest topic,  then callback
       // subIdList.add(this.restfulSubscribe());
        //jsonrpc subscribe
    //    subIdList.add(this.jsonRpcSubscribe());
        //mqtt subscribe
       // mqttPublish();
      //  this.mqttSubscribe();
        //subIdList.add(this.mqttSubscribe());
        // stomp subscribe
      //  subIdList.add(this.stomSubscribe());

        //Convert 'subIdList' to Json format
      /*  JSONObject jsonObject = new JSONObject();
        jsonObject.put(SUBSCRIBE_ID, subIdList);
        log.info(jsonObject.toJSONString());
        ScheduledService.writeStringToFile(subIdFile.getAbsolutePath(), jsonObject.toJSONString(), true);*/
    }

    private String restfulSubscribe()throws RestClientException {
        String callUrl = HTTP_HEADER+url+"/weevent/rest/subscribe?topic={topic}&subscriptionId={subscriptionId}&url={url}";
        String callBackUrl = HTTP_HEADER+url+"/weevent/mock/rest/onEvent";
        ResponseEntity<String> rsp = restTemplate.getForEntity(callUrl, String.class, "com.weevent.rest", "", callBackUrl);
        String subId = rsp.getBody();
        log.info("rest subId: " + subId);
        return  subId;
    }

    private String jsonRpcSubscribe() throws  BrokerException{
        String subId = weEventClient.subscribe("com.weevent.jsonrpc1", WeEvent.OFFSET_LAST, new IWeEventClient.EventListener() {
            @Override
            public void onEvent(WeEvent event) {
                log.info("jsonRpc subscribe "+event.toString());
            }

            @Override
            public void onException(Throwable e) {
                log.error(e.getMessage());
            }
        });
        log.info("jsonRpc subId: " + subId);
        return  subId;
    }

    private void mqttSubscribe()throws BrokerException{
     //   channelAdapter.start();
        String subId = "";
       //   mq.subscribeToMqtt("hello mqtt", "com.weevent.mqtt");
     //   subId = mqttBridge.subscribeTopic("com.weevent.mqtt.test","1");
        log.info("rest subId: " + subId);
     //   return subId;
    }

    private String stomSubscribe()throws InterruptedException, ExecutionException{
        StompHeaders header = new StompHeaders();
        header.setDestination("com.weevent.stomp");
        header.set("eventId","2cf24dba-59-1124");
        header.set("groupId","1");
        StompSessionHandlerAdapter stompSessionHandlerAdapter = getStompSessionHandlerAdapter();
        ListenableFuture<StompSession> connect = socketStompClient.connect("ws://localhost:8090/weevent/stomp", stompSessionHandlerAdapter, header, new Object());
        StompSession stompSession = connect.get();
        StompSession.Subscription subscribe = stompSession.subscribe("com.weevent.stomp", stompSessionHandlerAdapter);
        String subId = subscribe.getSubscriptionId();
        log.info("jsonRpc subId: " + subId);
        return  subId;
    }




    @Async
    @Scheduled(cron = "0/5 * * * * ?")
    public void scheduled() throws BrokerException {
        // use the rest request to post a message
        //restfulPublic();
        // use jsonRpc publish topic
      //  jsonrpcPublish();
        // use stomp publish topic
     //   stompPublish();
        // use mqtt publish topic
       mqttPublish();


    }


    public void restfulPublic() throws BrokerException {
        // use the rest request to post a message
        String callUrl = HTTP_HEADER+this.url+"/weevent/rest/open?topic={topic}&groupId={groupId}";
        Object result = restTemplate.getForEntity(callUrl,
            Object.class,
            "com.weevent.rest.test",
            "1").getBody();

        log.info("rest open result is " +result);

        callUrl = HTTP_HEADER+this.url+"/weevent/rest/publish?topic={topic}&content={content}";
        ResponseEntity<String> rsp = restTemplate.getForEntity(
            callUrl,
            String.class,
            "com.weevent.rest.test",
            "hello weevent rest");
        log.info("restful send message:" + rsp.getBody());
        if (rsp.getStatusCodeValue() == 200) {
            countTimes(restfulSendMap, this.getFormatTime(format, new Date()));
        }
    }

    public void jsonrpcPublish() throws BrokerException {
        // use jsonRpc publish topic
        String topic = "com.weevent.jsonrpc";
        weEventClient.open(topic);
        SendResult publish = weEventClient.publish(topic, "Hello World !".getBytes());
        log.info("jsonrpc send message:" + publish.getEventId());
        if (publish.getStatus() == SendResult.SendResultStatus.SUCCESS) {
            countTimes(jsonrpcSendMap, this.getFormatTime(format, new Date()));
        }
    }

    public void stompPublish() throws BrokerException {
        stompSession.send("com.weevent.stomp", "hello world from websocket");
        log.info("stomp send msg!");
        countTimes(stompSendMap, this.getFormatTime(format, new Date()));
    }

    public void mqttPublish()   {
        // Mqtt sends a message to weevent broker
        mqttGateway.sendToMqtt("hello mqtt", "com.weevent.mqtt");
        mqttGateway.subToMqtt("hello mqtt", "com.weevent.mqtt");
        log.info("mqtt send msg to broker");
        countTimes(mqttSendMap, this.getFormatTime(format, new Date()));
    }


    @Async
    @Scheduled(cron = "0 0 0/1 * * *")
    public void writeStatisticIntoFile() {
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.HOUR, -1);
        Date lastHour = calendar.getTime();
        String time = this.getFormatTime(format, lastHour);

        this.writeStringToFile(statisticFilePath, "Time is " + time + ":00:00\n", true);
        log.info(statisticFilePath, "Time is " + this.getFormatTime(format, date) + ":00:00\n");

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
        statisticMap.remove(time);
        restfulSendMap.remove(time);
        stompSendMap.remove(time);
        mqttSendMap.remove(time);
        jsonrpcSendMap.remove(time);


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
                String time = ScheduledService.getFormatTime(format, new Date());
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
       /*             try {
                        Thread.sleep(5000);
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
                        StompSession.Subscription subscribe = stompSession.subscribe("com.weevent.stomp", getStompFrameHander());
                    } catch (InterruptedException | ExecutionException e) {
                        log.error(e.getMessage());
                    }*/
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
        if (null != jsonObject && jsonObject.containsKey(SUBSCRIBE_ID)) {
            subIds = (List) jsonObject.get(SUBSCRIBE_ID);
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



    /**
     * get formatted time string
     *
     * @param format
     * @param date
     * @return
     */
    public static String getFormatTime(String format, Date date) {
        String formatTime = DateFormatUtils.format(date, format);
        return formatTime;
    }

    /**
     * Read the file to get the file content
     *
     * @param file
     * @return String
     */
    public static String readTxt(File file) throws IOException {
        if (!file.isFile() || !file.exists()) {
            file.createNewFile();
        }
        InputStream inputStream = new FileInputStream(file);
        String text = IOUtils.toString(inputStream, "utf8");
        return text;
    }

    /**
     * @param filePath is the file path,
     * @param content content needs to be written
     * @param flag is true for append, false for overwrite
     */
    public static void writeStringToFile(String filePath, String content, boolean flag) {
        File file = new File(filePath);
        if (!file.exists()) {
            try {
                file.createNewFile();
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

    @Override
    public void close() throws BrokerException {
        log.info("resource is close");
    }
}
