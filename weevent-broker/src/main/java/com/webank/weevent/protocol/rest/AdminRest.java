package com.webank.weevent.protocol.rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.webank.weevent.BrokerApplication;
import com.webank.weevent.broker.fisco.util.SystemInfoUtils;
import com.webank.weevent.broker.plugin.IConsumer;
import com.webank.weevent.sdk.BrokerException;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    private IConsumer consumer;

    @Autowired
    public void setConsumer(IConsumer consumer) {
        this.consumer = consumer;
    }

    public AdminRest() {
    }

    @RequestMapping(path = "/listSubscription")
    public Map<String, Object> listSubscription(@RequestParam(name = "groupId", required = false) String groupId) throws BrokerException {
        Map<String, Object> nodesInfo = new HashMap<>();
        if (this.masterJob.getClient() == null) {
            nodesInfo.put(SystemInfoUtils.getCurrentIp() + ":" + SystemInfoUtils.getCurrentPort(),
                    this.consumer.listSubscription(groupId));
        } else {
            try {
                List<String> ipList = this.masterJob.getClient().getChildren().forPath(BrokerApplication.weEventConfig.getZookeeperPath() + "/nodes");
                log.info("zookeeper ip List:{}", ipList);
                for (String nodeIP : ipList) {
                    byte[] ip = this.masterJob.getZookeeperNode(BrokerApplication.weEventConfig.getZookeeperPath() + "/nodes" + "/" + nodeIP);
                    SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
                    RestTemplate rest = new RestTemplate(requestFactory);
                    String url = new StringBuffer("http://").append(new String(ip)).append("/weevent/admin/innerListSubscription").toString();
                    if (!StringUtils.isBlank(groupId)){
                        url = new StringBuffer(url).append("?groupId=").append(groupId).toString();
                    }
                    log.info("url:{}", url);

                    ResponseEntity<String> rsp = rest.getForEntity(url, String.class);
                    log.debug("innerListSubscription:{}", JSON.parse(rsp.getBody()));
                    nodesInfo.put(new String(ip), JSON.parse(rsp.getBody()));
                }
            } catch (Exception e) {
                log.error(e.toString());
            }
        }


        return nodesInfo;
    }

    @RequestMapping(path = "/innerListSubscription")
    public Map<String, Object> innerListSubscription(@RequestParam(name = "groupId", required = false) String groupId) throws BrokerException {
        return this.consumer.listSubscription(groupId);
    }

}
