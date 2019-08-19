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
            Boolean result = rest.getForEntity("http://localhost:8080/weevent/rest/open?topic={}&groupId={}",
                    Boolean.class,
                    "com.weevent.test",
                    WeEvent.DEFAULT_GROUP_ID).getBody();
            System.out.println(result);
            // publish event to topic "com.weevent.test"
            SendResult sendResult = rest.getForEntity("http://localhost:8080/weevent/rest/publish?topic={}&groupId={}&content={}",
                    SendResult.class,
                    "com.weevent.test",
                    WeEvent.DEFAULT_GROUP_ID,
                    "hello weevent".getBytes(StandardCharsets.UTF_8)).getBody();
            System.out.println(sendResult.getStatus());
            System.out.println(sendResult.getEventId());
        } catch (RestClientException e) {
            e.printStackTrace();
        }
    }
}
