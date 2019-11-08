package com.webank.weevent.sample;


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
            IWeEventClient client = IWeEventClient.build("http://localhost:7000/weevent", WeEvent.DEFAULT_GROUP_ID);

            // ensure topic exist
            String topicName = "com.weevent.test";
            client.open(topicName);

            // publish "hello world" to topic
            WeEvent weEvent = new WeEvent(topicName, "hello WeEvent".getBytes());
            SendResult sendResult = client.publish(weEvent);
            System.out.println(sendResult);

            // subscribe topic
            String subscriptionId = client.subscribe(topicName, WeEvent.OFFSET_LAST, new IWeEventClient.EventListener() {
                @Override
                public void onEvent(WeEvent event) {
                    System.out.println("received event: " + event);
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
