package com.webank.weevent.gateway;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.webank.weevent.gateway.dto.NamingService;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.HttpServerErrorException;
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
    public void testListServices() {
        ResponseEntity<Map<String, List<NamingService>>> rsp = this.restTemplate.exchange(this.url + "/weevent-gateway/naming/list",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, List<NamingService>>>() {
                },
                new HashMap<>());

        Assert.assertEquals(200, rsp.getStatusCodeValue());
        Assert.assertNotNull(rsp.getBody());
        Assert.assertTrue(rsp.getBody().containsKey("weevent-gateway"));
        Assert.assertFalse(rsp.getBody().get("weevent-gateway").isEmpty());
    }

    @Test
    public void testBrokerGetVersion() {
        ResponseEntity<String> rsp = this.restTemplate.exchange(this.url + "/weevent-broker/admin/getVersion",
                HttpMethod.GET,
                null,
                String.class,
                new HashMap<>());
        log.info("http code status: {} body: {}", rsp.getStatusCode(), rsp.getBody());

        Assert.assertEquals(200, rsp.getStatusCodeValue());
        Assert.assertNotNull(rsp.getBody());
        Assert.assertFalse(rsp.getBody().isEmpty());
    }

    @Test
    public void testBrokerFileHostNotExist() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add("file_host", "not_exist");
            HttpEntity<?> requestEntity = new HttpEntity<>(headers);
            this.restTemplate.exchange(this.url + "/weevent-broker/admin/getVersion",
                    HttpMethod.GET,
                    requestEntity,
                    String.class,
                    new HashMap<>());

            Assert.fail();
        } catch (HttpServerErrorException.ServiceUnavailable e) {
            //503 Service Unavailable
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testBrokerFileHost() {
        ResponseEntity<Map<String, List<NamingService>>> services = this.restTemplate.exchange(this.url + "/weevent-gateway/naming/list",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, List<NamingService>>>() {
                },
                new HashMap<>());

        Assert.assertEquals(200, services.getStatusCodeValue());
        Assert.assertNotNull(services.getBody());
        Assert.assertTrue(services.getBody().containsKey("weevent-broker"));
        Assert.assertFalse(services.getBody().get("weevent-broker").isEmpty());

        HttpHeaders headers = new HttpHeaders();
        headers.add("file_host", services.getBody().get("weevent-broker").get(0).getInstanceId());
        HttpEntity<?> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<String> rsp = this.restTemplate.exchange(this.url + "/weevent-broker/admin/getVersion",
                HttpMethod.GET,
                requestEntity,
                String.class,
                new HashMap<>());

        Assert.assertEquals(200, rsp.getStatusCodeValue());
        Assert.assertNotNull(rsp.getBody());
        Assert.assertFalse(rsp.getBody().isEmpty());
    }
}
