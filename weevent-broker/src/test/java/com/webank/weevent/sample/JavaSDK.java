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
        String groupId = WeEvent.DEFAULT_GROUP_ID;
        String topicName = "com.weevent.test";
        String topicName2 = "com.weevent.test2";
        String[] topics = {topicName, topicName2};

        try {
            // get client
            IWeEventClient client = IWeEventClient.build("http://localhost:8080/weevent", groupId);
            // ensure topic exist
            client.open(topicName);
            client.open(topicName2);

            Map<String, String> extensions = new HashMap<>();
            extensions.put(WeEvent.WeEvent_FORMAT, "json");
            // publish an event to topic :"com.weevent.test"
            WeEvent weEvent = new WeEvent(topicName,"{\"hello\":\" wolrd\"}".getBytes(),extensions);
            SendResult sendResult = client.publish(weEvent);
            System.out.println("publish an event to topic: " + topicName + ", result: " + sendResult);
            // publish an event to topic :"com.weevent.test2"
            WeEvent weEvent2 = new WeEvent(topicName2,"{\"hello\":\" wolrd\"}".getBytes(),extensions);
            SendResult sendResult2 = client.publish(weEvent2);
            System.out.println("publish an event to topic: " + topicName2 + ", result: " + sendResult2);

            // single topic subscribe
            singleTopicSubscribe(client, topicName, WeEvent.OFFSET_LAST);

            // multiple topic subscribe
            multipleTopicSubscribe(client, topics, WeEvent.OFFSET_LAST);

        } catch (BrokerException e) {
            e.printStackTrace();
        }

    }

    public static void singleTopicSubscribe(IWeEventClient client, String topic, String offset) throws BrokerException {
        String subscriptionId = client.subscribe(topic, offset, new IWeEventClient.EventListener() {
            @Override
            public void onEvent(WeEvent event) {
                System.out.println("received event: " + event.toString());
            }

            @Override
            public void onException(Throwable e) {

            }
        });
        System.out.println("single topic subscribe, subscriptionId: "+subscriptionId);

        // unSubscribe topic
        client.unSubscribe(subscriptionId);
    }

    public static void multipleTopicSubscribe(IWeEventClient client, String[] topics, String offset) throws BrokerException {

        String subscriptionId = client.subscribe(topics, offset, new IWeEventClient.EventListener() {
            @Override
            public void onEvent(WeEvent event) {
                System.out.println("received event: " + event.toString());
            }

            @Override
            public void onException(Throwable e) {

            }
        });
        System.out.println("multiple topics subscribe, subscriptionId: "+subscriptionId);

        // unSubscribe topic
        client.unSubscribe(subscriptionId);
    }
}
