package com.webank.weevent.sample;

import java.nio.charset.StandardCharsets;

import com.webank.weevent.sdk.SendResult;
import com.webank.weevent.sdk.WeEvent;

import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

public class Rest {
    public static void main(String[] args) {
        System.out.println("This is WeEvent restful sample.");
        try {
            SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
            RestTemplate rest = new RestTemplate(requestFactory);

            // ensure topic exist "com.weevent.test"
            String topic = "com.weevent.test";
            Boolean result = rest.getForEntity("http://localhost:7000/weevent/rest/open?topic={topic}&groupId={groupId}",
                    Boolean.class,
                    topic,
                    WeEvent.DEFAULT_GROUP_ID).getBody();
            System.out.println(result);

            // publish event to topic "com.weevent.test"
            SendResult sendResult = rest.getForEntity("http://localhost:7000/weevent/rest/publish?topic={topic}&groupId={groupId}&content={content}",
                    SendResult.class,
                    topic,
                    WeEvent.DEFAULT_GROUP_ID,
                    "hello WeEvent".getBytes(StandardCharsets.UTF_8)).getBody();
            System.out.println(sendResult);
        } catch (RestClientException e) {
            e.printStackTrace();
        }
    }
}
