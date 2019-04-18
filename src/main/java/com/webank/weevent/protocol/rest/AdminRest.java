package com.webank.weevent.protocol.rest;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.webank.weevent.BrokerApplication;
import com.webank.weevent.broker.fisco.dto.FiscoBlockChainInfo;
import com.webank.weevent.broker.fisco.service.impl.BlockChainServiceImpl;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.ErrorCode;
import com.webank.weevent.broker.plugin.IConsumer;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * @author websterchen
 * @version 1.0
 * @since 2018/12/14
 */
@RequestMapping(value = "/admin")
@RestController
@Slf4j
public class AdminRest extends RestHA {
    private BlockChainServiceImpl blockChainService;
    private IConsumer consumer;

    @Autowired
    public void setConsumer(IConsumer consumer) {
        this.consumer = consumer;
    }

    public AdminRest() {
        this.blockChainService = new BlockChainServiceImpl();
    }

    @RequestMapping(path = "/blockchaininfo", method = RequestMethod.GET)
    public FiscoBlockChainInfo getBlockInfo() throws BrokerException, IOException {
        FiscoBlockChainInfo fiscoBlockChainInfo = new FiscoBlockChainInfo();
        fiscoBlockChainInfo.setBlockNumber(this.blockChainService.getBlockHeight());
        fiscoBlockChainInfo.setNodeIdList(this.blockChainService.getNodeInfo().get("nodeId"));
        fiscoBlockChainInfo.setNodeIpList(this.blockChainService.getNodeInfo().get("nodeIp"));
        return fiscoBlockChainInfo;
    }

    @RequestMapping(path = "/deploy_topic_control", method = RequestMethod.GET)
    public String deployTopicControl() throws BrokerException {
        return this.blockChainService.deployTopicContracts();
    }

    @RequestMapping(path = "/listSubscription")
    public Map<String, Object> listSubscription() throws BrokerException {
        if (this.masterJob.getClient() == null) {
            log.error("no broker.zookeeper.ip configuration, skip it");
            throw new BrokerException(ErrorCode.CGI_SUBSCRIPTION_NO_ZOOKEEPER);
        }

        Map<String, Object> nodesInfo = new HashMap<>();
        try {
            List<String> ipList = this.masterJob.getClient().getChildren().forPath(BrokerApplication.weEventConfig.getZookeeperPath() + "/nodes");
            log.info("zookeeper ip List:{}",ipList);
            for (String nodeip : ipList) {
                byte[] ip = this.masterJob.getZookeeperNode(BrokerApplication.weEventConfig.getZookeeperPath() + "/nodes" + "/" + nodeip);
                SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
                RestTemplate rest = new RestTemplate(requestFactory);
                String url = "";
                if (BrokerApplication.weEventConfig.getSslEnable().equals("true")) {
                    url = "https://" + new String(ip) + "/weevent/admin/innerListSubscription";
                } else {
                    url = "http://" + new String(ip) + "/weevent/admin/innerListSubscription";
                    //url = "http://127.0.0.1:8081/weevent/admin/innerListSubscription";
                }
                log.info("url:{}", url);
                ResponseEntity<String> rsp = rest.getForEntity(url, String.class);
                log.debug("innerListSubscription:{}", JSON.parse(rsp.getBody()));
                nodesInfo.put(new String(ip), JSON.parse(rsp.getBody()));
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return nodesInfo;
    }

    @RequestMapping(path = "/innerListSubscription")
    public Map<String, Object> innerListSubscription() throws BrokerException {
        return consumer.getInnerSubscription();
    }
}
