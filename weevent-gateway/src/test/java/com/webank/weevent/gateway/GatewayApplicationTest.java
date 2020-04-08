package com.webank.weevent.gateway;

import java.util.HashMap;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * GatewayApplication Tester.
 *
 * @author <matthewliu>
 * @version 1.0
 * @since <pre>04/08/2020</pre>
 */
@Slf4j
public class GatewayApplicationTest extends JUnitTestBase {
    private final String url = "http://localhost:8080";

    private RestTemplate restTemplate;

    @Before
    public void before() {
        log.info("=============================={}.{}==============================",
                this.getClass().getSimpleName(),
                this.testName.getMethodName());

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        this.restTemplate = new RestTemplate(requestFactory);
    }

    @Test
    public void testBrokerGetVersion() {
        ResponseEntity<String> rsp = this.restTemplate.exchange(this.url + "/weevent-broker/admin/getVersion",
                HttpMethod.GET,
                null,
                String.class,
                new HashMap<>());
        log.info("status: " + rsp.getStatusCode() + " body: " + rsp.getBody());

        Assert.assertEquals(200, rsp.getStatusCodeValue());
        Assert.assertNotNull(rsp.getBody());
        Assert.assertFalse(rsp.getBody().isEmpty());
    }

    @Test
    public void testBrokerExistTopic() {
        ResponseEntity<String> rsp = this.restTemplate.exchange(this.url + "/weevent-broker/rest/exist?topic=not_exist",
                HttpMethod.GET,
                null,
                String.class,
                new HashMap<>());
        log.info("status: " + rsp.getStatusCode() + " body: " + rsp.getBody());

        Assert.assertEquals(200, rsp.getStatusCodeValue());
        Assert.assertNotNull(rsp.getBody());
        Assert.assertFalse(rsp.getBody().isEmpty());
    }
}
