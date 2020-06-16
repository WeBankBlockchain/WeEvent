package com.webank.weevent.protocol.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.webank.weevent.BrokerApplication;
import com.webank.weevent.broker.fisco.dto.SubscriptionInfo;
import com.webank.weevent.broker.fisco.util.SystemInfoUtils;
import com.webank.weevent.broker.plugin.IConsumer;
import com.webank.weevent.sdk.JsonHelper;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.bind.annotation.RequestMapping;
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
    public Map<String, List<SubscriptionInfo>> listSubscription() {
        String url = "";
        Map<String, List<SubscriptionInfo>> nodesInfo = new HashMap<>();
        if (this.masterJob.getClient() == null) {
            nodesInfo.put(SystemInfoUtils.getCurrentIp() + ":" + SystemInfoUtils.getCurrentPort(),
                    new ArrayList<>(this.consumer.listSubscription().values()));
        } else {
            try {
                List<String> ipList = this.masterJob.getClient().getChildren().forPath(BrokerApplication.weEventConfig.getZookeeperPath() + "/nodes");
                log.info("zookeeper ip List:{}", ipList);
                for (String nodeip : ipList) {
                    byte[] ip = this.masterJob.getZookeeperNode(BrokerApplication.weEventConfig.getZookeeperPath() + "/nodes" + "/" + nodeip);
                    SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
                    RestTemplate rest = new RestTemplate(requestFactory);
                    url = "http://" + new String(ip) + "/weevent/admin/innerListSubscription";
                    log.info("url:{}", url);

                    ResponseEntity<String> rsp = rest.getForEntity(url, String.class);
                    Map<String, SubscriptionInfo> data = JsonHelper.json2Object(rsp.getBody(), new TypeReference<Map<String, SubscriptionInfo>>() {
                    });
                    log.debug("innerListSubscription:{}", data);
                    nodesInfo.put(new String(ip), new ArrayList<>(data.values()));
                }
            } catch (Exception e) {
                log.error(e.toString());
            }
        }


        return nodesInfo;
    }

    @RequestMapping(path = "/innerListSubscription")
    public Map<String, SubscriptionInfo> innerListSubscription() {
        return this.consumer.listSubscription();
    }
}
