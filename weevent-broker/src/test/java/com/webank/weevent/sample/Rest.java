package com.webank.weevent.sample;

import java.nio.charset.StandardCharsets;

import com.webank.weevent.sdk.SendResult;

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
            Boolean result = rest.getForEntity("http://localhost:8080/weevent/rest/open?topic={}",
                    Boolean.class,
                    "com.weevent.test").getBody();
            System.out.println(result);

            // publish event to topic "com.weevent.test"
            SendResult sendResult = rest.getForEntity("http://localhost:8080/weevent/rest/publish?topic={}&content={}",
                    SendResult.class,
                    "com.weevent.test",
                    "hello weevent".getBytes(StandardCharsets.UTF_8)).getBody();

            System.out.println(sendResult.getStatus());
            System.out.println(sendResult.getEventId());
        } catch (RestClientException e) {
            e.printStackTrace();
        }
    }
}
