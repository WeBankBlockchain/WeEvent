package com.webank.weevent.sample;


import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.IWeEventClient;
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
            IWeEventClient client = IWeEventClient.build("http://localhost:8080/weevent");

            // ensure topic exist
            client.open(topicName, groupId);
            Map<String, String> extensions = new HashMap<>();
            extensions.put("weevent-url", "https://github.com/WeBankFinTech/WeEvent");
            // subscribe topic with groupId
            String subscriptionId = client.subscribe(topicName, groupId, WeEvent.OFFSET_LAST, new WeEventClient.EventListener() {
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
                client.publish(topicName, groupId, ("hello weevent: " + i).getBytes(StandardCharsets.UTF_8), extensions);
            }

            // unSubscribe topic
            client.unSubscribe(subscriptionId);
        } catch (BrokerException e) {
            e.printStackTrace();
        }
    }
}
