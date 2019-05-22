package com.webank.weevent.sample;


import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.WeEvent;
import com.webank.weevent.sdk.WeEventClient;

/**
 * Sample of Java SDK.
 *
 * @author matthewliu
 * @since 2019/04/07
 */
public class JavaSDK {
    private final static String topicName = "com.weevent.test";
    private final static String groupId = "1";
    private final static Map<String, String> extensions = new HashMap<>();

    public static void main(String[] args) {
        System.out.println("This is WeEvent Java SDK sample.");
        try {
            // get client
            WeEventClient client = new WeEventClient("http://localhost:8080/weevent");

            // ensure topic exist
            client.open(topicName, groupId);

            // subscribe topic
            String subscriptionId = client.subscribe(topicName, WeEvent.OFFSET_LAST, new WeEventClient.EventListener() {
                @Override
                public void receiptId2SubscriptionId(WeEvent event) {
                    System.out.println("received event: " + event.toString());
                }

                @Override
                public void onException(Throwable e) {

                }
            });

            // publish event
            for (int i = 0; i < 10; i++) {
                client.publish(topicName, groupId, ("hello weevent: " + i).getBytes(StandardCharsets.UTF_8), extensions);
            }

            // unSubscribe topic
            client.unSubscribe(subscriptionId);
        } catch (BrokerException e) {
            e.printStackTrace();
        }
    }
}
