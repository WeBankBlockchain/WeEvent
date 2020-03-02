package com.webank.weevent.broker.st;

import java.util.List;

import com.webank.weevent.broker.JUnitTestBase;
import com.webank.weevent.broker.protocol.rest.ResponseData;
import com.webank.weevent.sdk.JsonHelper;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Slf4j
public class RestfullAdminTest extends JUnitTestBase {

    private String url;
    private RestTemplate admin = null;

    @Before
    public void before() {
        log.info("=============================={}.{}==============================",
                this.getClass().getSimpleName(),
                this.testName.getMethodName());

        url = "http://localhost:7000/weevent-broker/admin/";
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        this.admin = new RestTemplate(requestFactory);
    }

    @Test
    public void testGetVersion() {
        ResponseEntity<ResponseData> rsp = admin.getForEntity(url + "getVersion", ResponseData.class);
        log.info("getVersion, status: " + rsp.getStatusCode() + " body: " + rsp.getBody());
        Assert.assertTrue(rsp.getStatusCodeValue() == 200);
        Assert.assertTrue(rsp.getBody().getCode() == 0);
        Assert.assertTrue(rsp.getBody().getData() != null);
    }

    @Test
    public void testListNodes() {
        ResponseEntity<ResponseData> rsp = admin.getForEntity(url + "listNodes", ResponseData.class);
        log.info("listNodes, status: " + rsp.getStatusCode() + " body: " + rsp.getBody());
        Object data = rsp.getBody().getData();
        List<String> nodes = JsonHelper.object2List(data, String.class);
        Assert.assertTrue(rsp.getStatusCodeValue() == 200);
        Assert.assertTrue(rsp.getBody().getCode() == 0);
        Assert.assertTrue(!nodes.isEmpty());
    }

    @Test
    public void testListSubscription() {
        ResponseEntity<ResponseData> rsponse = admin.getForEntity(url + "listNodes", ResponseData.class);
        Object data = rsponse.getBody().getData();
        List<String> nodes = JsonHelper.object2List(data, String.class);
        Assert.assertTrue(!nodes.isEmpty());

        ResponseEntity<ResponseData> rsp = admin.getForEntity(url + "listSubscription?groupId={groupId}&nodeIp={nodeIp}", ResponseData.class, this.groupId, nodes.get(0));
        log.info("listSubscription, status: " + rsp.getStatusCode() + " body: " + rsp.getBody());
        Assert.assertTrue(rsp.getStatusCodeValue() == 200);
        Assert.assertTrue(rsp.getBody().getCode() == 0);
        Assert.assertTrue(rsp.getBody().getData() != null);
    }

    @Test
    public void testGetGroupGeneral() {
        ResponseEntity<ResponseData> rsp = admin.getForEntity(url + "group/general", ResponseData.class);
        log.info("get group general, status: " + rsp.getStatusCode() + " body: " + rsp.getBody());
        Assert.assertTrue(rsp.getStatusCodeValue() == 200);
        Assert.assertTrue(rsp.getBody().getCode() == 0);
        Assert.assertTrue(rsp.getBody().getData() != null);
    }

    @Test
    public void testQueryTransList() {
        ResponseEntity<ResponseData> rsp = admin.getForEntity(url + "transaction/transList?pageNumber={pageNumber}&pageSize={pageSize}", ResponseData.class, 1, 10);
        log.info("get transaction list, status: " + rsp.getStatusCode() + " body: " + rsp.getBody());
        Assert.assertTrue(rsp.getStatusCodeValue() == 200);
        Assert.assertTrue(rsp.getBody().getCode() == 0);
        Assert.assertTrue(rsp.getBody().getData() != null);
    }

    @Test
    public void testQueryBlockList() {
        ResponseEntity<ResponseData> rsp = admin.getForEntity(url + "block/blockList?pageNumber={pageNumber}&pageSize={pageSize}", ResponseData.class, 1, 10);
        log.info("get blockList list, status: " + rsp.getStatusCode() + " body: " + rsp.getBody());
        Assert.assertTrue(rsp.getStatusCodeValue() == 200);
        Assert.assertTrue(rsp.getBody().getCode() == 0);
        Assert.assertTrue(rsp.getBody().getData() != null);
    }

    @Test
    public void testQueryNodeList() {
        ResponseEntity<ResponseData> rsp = admin.getForEntity(url + "node/nodeList?pageNumber={pageNumber}&pageSize={pageSize}", ResponseData.class, 1, 10);
        log.info("get node list, status: " + rsp.getStatusCode() + " body: " + rsp.getBody());
        Assert.assertTrue(rsp.getStatusCodeValue() == 200);
        Assert.assertTrue(rsp.getBody().getCode() == 0);
        Assert.assertTrue(rsp.getBody().getData() != null);
    }

    @Test
    public void testListGroupId() {
        ResponseEntity<ResponseData> rsp = admin.getForEntity(url + "listGroup", ResponseData.class);
        Assert.assertTrue(rsp.getStatusCodeValue() == 200);
        Assert.assertTrue(rsp.getBody().getCode() == 0);
        Assert.assertTrue(rsp.getBody().getData() != null);
    }

    @Test
    public void testContractContext() {
        ResponseEntity<ResponseData> rsp = admin.getForEntity(url + "getContractContext", ResponseData.class);
        Assert.assertEquals(200, rsp.getStatusCodeValue());
        Assert.assertEquals(0, rsp.getBody().getCode());
        Assert.assertNotNull(rsp.getBody().getData());
    }

}
