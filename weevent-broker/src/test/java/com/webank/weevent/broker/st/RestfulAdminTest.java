package com.webank.weevent.broker.st;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.webank.weevent.broker.JUnitTestBase;
import com.webank.weevent.client.BaseResponse;
import com.webank.weevent.client.WeEvent;
import com.webank.weevent.core.dto.SubscriptionInfo;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Slf4j
public class RestfulAdminTest extends JUnitTestBase {
    private final String groupId = WeEvent.DEFAULT_GROUP_ID;

    private String url;
    private RestTemplate admin = null;

    @Before
    public void before() {
        log.info("=============================={}.{}==============================",
                this.getClass().getSimpleName(),
                this.testName.getMethodName());

        this.url = "http://localhost:" + this.listenPort + "/weevent-broker/admin/";

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        this.admin = new RestTemplate(requestFactory);
    }

    @Test
    public void testGetVersion() {
        ResponseEntity<BaseResponse> rsp = admin.getForEntity(url + "getVersion", BaseResponse.class);
        log.info("getVersion, status: " + rsp.getStatusCode() + " body: " + rsp.getBody());

        Assert.assertEquals(200, rsp.getStatusCodeValue());
        Assert.assertNotNull(rsp.getBody());
        Assert.assertEquals(0, rsp.getBody().getCode());
        Assert.assertNotNull(rsp.getBody().getData());
    }

    @Test
    public void testListNodes() {
        ResponseEntity<BaseResponse<List<String>>> rsp = admin.exchange(url + "listNodes", HttpMethod.GET, null, new ParameterizedTypeReference<BaseResponse<List<String>>>() {
        });
        log.info("listNodes, status: " + rsp.getStatusCode() + " body: " + rsp.getBody());

        Assert.assertEquals(200, rsp.getStatusCodeValue());
        Assert.assertNotNull(rsp.getBody());
        List<String> nodes = rsp.getBody().getData();
        Assert.assertEquals(0, rsp.getBody().getCode());
        Assert.assertFalse(nodes.isEmpty());
    }

    @Test
    public void testListSubscription() {
        ResponseEntity<BaseResponse<List<String>>> response = admin.exchange(url + "listNodes",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<BaseResponse<List<String>>>() {
                });
        Assert.assertEquals(200, response.getStatusCodeValue());
        Assert.assertNotNull(response.getBody());
        List<String> nodes = response.getBody().getData();
        Assert.assertFalse(nodes.isEmpty());

        Map<String, String> params = new HashMap<>();
        params.put("groupId", groupId);
        params.put("nodeInstances", nodes.get(0));
        ResponseEntity<BaseResponse<Map<String, List<SubscriptionInfo>>>> rsp = admin.exchange(url + "listSubscription?groupId={groupId}&nodeInstances={nodeInstances}",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<BaseResponse<Map<String, List<SubscriptionInfo>>>>() {
                },
                params);
        log.info("listSubscription, status: " + rsp.getStatusCode() + " body: " + rsp.getBody());

        Assert.assertEquals(200, rsp.getStatusCodeValue());
        Assert.assertNotNull(rsp.getBody());
        Assert.assertEquals(0, rsp.getBody().getCode());
    }

    @Test
    public void testGetGroupGeneral() {
        ResponseEntity<BaseResponse> rsp = admin.getForEntity(url + "group/general", BaseResponse.class);
        log.info("get group general, status: " + rsp.getStatusCode() + " body: " + rsp.getBody());

        Assert.assertEquals(200, rsp.getStatusCodeValue());
        Assert.assertNotNull(rsp.getBody());
        Assert.assertEquals(0, rsp.getBody().getCode());
        Assert.assertNotNull(rsp.getBody().getData());
    }

    @Test
    public void testQueryTransList() {
        ResponseEntity<BaseResponse> rsp = admin.getForEntity(url + "transaction/transList?pageNumber={pageNumber}&pageSize={pageSize}", BaseResponse.class, 1, 10);
        log.info("get transaction list, status: " + rsp.getStatusCode() + " body: " + rsp.getBody());

        Assert.assertEquals(200, rsp.getStatusCodeValue());
        Assert.assertNotNull(rsp.getBody());
        Assert.assertEquals(0, rsp.getBody().getCode());
        Assert.assertNotNull(rsp.getBody().getData());
    }

    @Test
    public void testQueryBlockList() {
        ResponseEntity<BaseResponse> rsp = admin.getForEntity(url + "block/blockList?pageNumber={pageNumber}&pageSize={pageSize}", BaseResponse.class, 1, 10);
        log.info("get blockList list, status: " + rsp.getStatusCode() + " body: " + rsp.getBody());

        Assert.assertEquals(200, rsp.getStatusCodeValue());
        Assert.assertNotNull(rsp.getBody());
        Assert.assertEquals(0, rsp.getBody().getCode());
        Assert.assertNotNull(rsp.getBody().getData());
    }

    @Test
    public void testQueryNodeList() {
        ResponseEntity<BaseResponse> rsp = admin.getForEntity(url + "node/nodeList?pageNumber={pageNumber}&pageSize={pageSize}", BaseResponse.class, 1, 10);
        log.info("get node list, status: " + rsp.getStatusCode() + " body: " + rsp.getBody());

        Assert.assertEquals(200, rsp.getStatusCodeValue());
        Assert.assertNotNull(rsp.getBody());
        Assert.assertEquals(0, rsp.getBody().getCode());
        Assert.assertNotNull(rsp.getBody().getData());
    }

    @Test
    public void testListGroupId() {
        ResponseEntity<BaseResponse> rsp = admin.getForEntity(url + "listGroup", BaseResponse.class);

        Assert.assertEquals(200, rsp.getStatusCodeValue());
        Assert.assertNotNull(rsp.getBody());
        Assert.assertEquals(0, rsp.getBody().getCode());
        Assert.assertNotNull(rsp.getBody().getData());
    }

    @Test
    public void testContractContext() {
        ResponseEntity<BaseResponse> rsp = admin.getForEntity(url + "getContractContext", BaseResponse.class);

        Assert.assertEquals(200, rsp.getStatusCodeValue());
        Assert.assertNotNull(rsp.getBody());
        Assert.assertEquals(0, rsp.getBody().getCode());
        Assert.assertNotNull(rsp.getBody().getData());
    }

    @Test
    public void testValidateGroupId() {
        ResponseEntity<BaseResponse> rsp = admin.getForEntity(url + "validateGroupId?groupId={groupId}", BaseResponse.class, groupId);

        Assert.assertEquals(200, rsp.getStatusCodeValue());
        Assert.assertNotNull(rsp.getBody());
        Assert.assertEquals(0, rsp.getBody().getCode());
        Assert.assertNotNull(rsp.getBody().getData());
    }
}
