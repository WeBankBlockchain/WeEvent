package com.webank.weevent.broker.sample;

import java.nio.charset.StandardCharsets;

import com.webank.weevent.client.BaseResponse;
import com.webank.weevent.client.SendResult;
import com.webank.weevent.client.WeEvent;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
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
            ResponseEntity<BaseResponse<Boolean>> rsp = rest.exchange("http://localhost:7000/weevent-broker/rest/open?topic={topic}&groupId={groupId}", HttpMethod.GET, null, new ParameterizedTypeReference<BaseResponse<Boolean>>() {
            }, topic, WeEvent.DEFAULT_GROUP_ID);
            System.out.println(rsp.getBody().getData());

            // publish event to topic "com.weevent.test"
            SendResult sendResult = rest.getForEntity("http://localhost:7000/weevent-broker/rest/publish?topic={topic}&groupId={groupId}&content={content}",
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
