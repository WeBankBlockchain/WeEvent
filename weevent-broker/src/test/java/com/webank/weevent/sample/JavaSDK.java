package com.webank.weevent.sample;


import java.nio.charset.StandardCharsets;

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

    public static void main(String[] args) {
        System.out.println("This is WeEvent Java SDK sample.");

        try {
            // get client
            WeEventClient client = new WeEventClient("http://localhost:8080/weevent");

            // ensure topic exist
            client.open(topicName);

            // subscribe topic
            String subscriptionId = client.subscribe(topicName, WeEvent.OFFSET_LAST, new WeEventClient.EventListener() {
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
                client.publish(topicName, ("hello weevent: " + i).getBytes(StandardCharsets.UTF_8));
            }

            // unSubscribe topic
            client.unSubscribe(subscriptionId);
        } catch (BrokerException e) {
            e.printStackTrace();
        }
    }
}
