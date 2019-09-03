package com.webank.weevent.sample;


import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.IWeEventClient;
import com.webank.weevent.sdk.SendResult;
import com.webank.weevent.sdk.WeEvent;

/**
 * Sample of Java SDK.
 *
 * @author matthewliu
 * @since 2019/04/07
 */
public class JavaSDK {
    public static void main(String[] args) {
        System.out.println("This is WeEvent Java SDK sample.");
        try {
            // get client
            IWeEventClient client = IWeEventClient.build("http://localhost:8080/weevent");
            String topicName = "com.weevent.test";
            String groupId = WeEvent.DEFAULT_GROUP_ID;
            // ensure topic exist
            client.open(topicName, groupId);
            Map<String, String> extensions = new HashMap<>();
            extensions.put(WeEvent.WeEvent_FORMAT, "json");
            WeEvent weEvent = new WeEvent();
            weEvent.setTopic(topicName);
            weEvent.setContent("{\"hello\":\" wolrd\"}".getBytes());
            weEvent.setExtensions(extensions);
            SendResult sendResult = client.publish(weEvent, groupId);
            System.out.println(sendResult.toString());
            // subscribe topic with groupId
            String subscriptionId = client.subscribe(topicName, groupId, WeEvent.OFFSET_LAST, new IWeEventClient.EventListener() {
                @Override
                public void onEvent(WeEvent event) {
                    System.out.println("received event: " + event.toString());
                }

                @Override
                public void onException(Throwable e) {

                }
            });

            // publish event
            for (int i = 0; i < 10; i++) {
                weEvent.setContent(("hello weevent: " + i).getBytes(StandardCharsets.UTF_8));
                client.publish(weEvent, groupId);
            }

            // unSubscribe topic
            client.unSubscribe(subscriptionId);
        } catch (BrokerException e) {
            e.printStackTrace();
        }
    }
}
