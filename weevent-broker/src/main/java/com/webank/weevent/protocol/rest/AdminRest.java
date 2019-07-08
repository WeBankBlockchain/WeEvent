package com.webank.weevent.protocol.rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.webank.weevent.BrokerApplication;
import com.webank.weevent.broker.fisco.util.SystemInfoUtils;
import com.webank.weevent.broker.plugin.IConsumer;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.ErrorCode;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.SystemUtils;
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
    public Map<String, Object> listSubscription() throws BrokerException {
        String url = "";
        Map<String, Object> nodesInfo = new HashMap<>();
        if (this.masterJob.getClient() == null) {
            String port = BrokerApplication.weEventConfig.getServerPort();
            String ip = SystemInfoUtils.getCurrentIp() +":" + port;
            nodesInfo.put(new String(ip), this.consumer.listSubscription());
        } else {
            try {
                List<String> ipList = this.masterJob.getClient().getChildren().forPath(BrokerApplication.weEventConfig.getZookeeperPath() + "/nodes");
                log.info("zookeeper ip List:{}", ipList);
                for (String nodeip : ipList) {
                    byte[] ip = this.masterJob.getZookeeperNode(BrokerApplication.weEventConfig.getZookeeperPath() + "/nodes" + "/" + nodeip);
                    SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
                    RestTemplate rest = new RestTemplate(requestFactory);
                    if (BrokerApplication.weEventConfig.getSslEnable().equals("true")) {
                        url = "https://" + new String(ip) + "/weevent/admin/innerListSubscription";
                    } else {
                        url = "http://" + new String(ip) + "/weevent/admin/innerListSubscription";
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
    public Map<String, Object> innerListSubscription() throws BrokerException {
        return this.consumer.listSubscription();
    }
}
