package com.webank.weevent.sample;


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
            String groupId = WeEvent.DEFAULT_GROUP_ID;
            // get client
            IWeEventClient client = IWeEventClient.build("http://localhost:8080/weevent", groupId);
            String topicName = "com.weevent.test";

            // ensure topic exist
            client.open(topicName);
            Map<String, String> extensions = new HashMap<>();
            extensions.put(WeEvent.WeEvent_FORMAT, "json");
            WeEvent weEvent = new WeEvent(topicName,"{\"hello\":\" wolrd\"}".getBytes(),extensions);
            // publish an event to topic :"com.weevent.test"
            SendResult sendResult = client.publish(weEvent);
            System.out.println(sendResult.toString());

            // subscribe topic
            String subscriptionId = client.subscribe(topicName, WeEvent.OFFSET_LAST, new IWeEventClient.EventListener() {
                @Override
                public void onEvent(WeEvent event) {
                    System.out.println("received event: " + event.toString());
                }

                @Override
                public void onException(Throwable e) {

                }
            });

            // unSubscribe topic
            client.unSubscribe(subscriptionId);
        } catch (BrokerException e) {
            e.printStackTrace();
        }
    }
}
