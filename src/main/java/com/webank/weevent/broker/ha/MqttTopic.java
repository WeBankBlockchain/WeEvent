package com.webank.weevent.broker.ha;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.webank.weevent.BrokerApplication;
import com.webank.weevent.broker.fisco.util.SerializeUtils;
import com.webank.weevent.protocol.mqtt.MqttBridge;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.ErrorCode;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.web.client.RestTemplate;

/**
 * MQTT topic in local cache and zookeeper.
 *
 * @author matthewliu
 * @since 2019/03/13
 */
@Slf4j
public class MqttTopic {
    //mqtt handler
    private MqttPahoMessageDrivenChannelAdapter mqttPahoMessageDrivenChannelAdapter;
    private MqttBridge mqttBridge;

    // zookeeper state
    private MasterJob masterJob;
    private boolean isMaster;
    private String zookeeperPath;
    private final static Integer MQTT_IN_BOUND = 0;
    private final static Integer MQTT_OUT_BOUND = 1;
    // topic name <-> ZKSubscription
    // used to log only, no others
    private List<Map<String, ZKSubscription>> topics;

    MqttTopic(MasterJob masterJob, String path) {
        this.masterJob = masterJob;
        // default in slave mode
        this.isMaster = false;
        this.zookeeperPath = path;

        // depend mqtt.broker.url
        if (BrokerApplication.applicationContext.containsBean("mqttInbound")) {
            this.mqttPahoMessageDrivenChannelAdapter = (MqttPahoMessageDrivenChannelAdapter) BrokerApplication.applicationContext.getBean("mqttInbound");
        }

        // depend mqtt.broker.url
        if (BrokerApplication.applicationContext.containsBean("MqttBridgeHandler")) {
            this.mqttBridge = ((MqttBridge) BrokerApplication.applicationContext.getBean("MqttBridgeHandler"));
        }
    }

    /*
    PathChildrenCache can be used to maintain a full local cache, but it too difficulty.
    Notice as followings:
    1: DOT NOT support recursive.
    2: Notify events with all data in zookeeper, doesn't matter it is new or old.
    Flush data from zookeeper when initialize is more easy, and then only master can update data in zookeeper.
     */
    private Map<String, ZKSubscription> flushNode(String path) {
        try {
            log.info("start to flush node, {}", path);

            Map<String, ZKSubscription> nodes = new HashMap<>();
            List<String> childNodes = this.masterJob.getClient().getChildren().forPath(path);
            log.info("node list: {}", childNodes);
            for (String node : childNodes) {
                byte[] data = this.masterJob.getZookeeperNode(path + "/" + node);
                ZKSubscription zkSubscription = SerializeUtils.deserialize(data);
                nodes.put(zkSubscription.getTopic(), zkSubscription);
            }
            return nodes;
        } catch (Exception e) {
            log.error("flush node failed", e);
            return null;
        }
    }

    public boolean switchMode(boolean master) {
        if (this.mqttPahoMessageDrivenChannelAdapter == null || this.mqttBridge == null) {
            log.info("no broker url, skip mqtt");
            return true;
        }

        this.isMaster = master;
        if (!this.isMaster) {
            log.info("switch in slave mode");
            this.topics = null;
            return true;
        }

        // if in master mode, flush subscriptions from zookeeper into memory
        log.info("switch in master mode");
        this.topics = new ArrayList<>();

        String inBoundPath = this.zookeeperPath + "/inbound";
        Map<String, ZKSubscription> inNodes = flushNode(inBoundPath);
        if (inNodes == null) {
            return false;
        }
        this.topics.add(MQTT_IN_BOUND, inNodes);

        String outBoundPath = this.zookeeperPath + "/outbound";
        Map<String, ZKSubscription> outNodes = flushNode(outBoundPath);
        if (outNodes == null) {
            return false;
        }

        this.topics.add(MQTT_OUT_BOUND, outNodes);
        return true;
    }

    private void checkSupport() throws BrokerException {
        if (this.mqttPahoMessageDrivenChannelAdapter == null || this.mqttBridge == null) {
            log.error("no mqtt.broker.url configuration, skip it");
            throw new BrokerException(ErrorCode.MQTT_NO_BROKER_URL);
        }
    }

    public boolean mqttAddInBoundTopic(String topic, String urlFormat) throws BrokerException {
        log.info("add mqtt inbound topic: {}", topic);

        checkSupport();

        if (this.isMaster) {
            log.info("i am leader, do it directly");

            this.mqttBridge.assertExist(topic);

            this.mqttPahoMessageDrivenChannelAdapter.addTopic(topic, BrokerApplication.weEventConfig.getMqttBrokerQos());

            ZKSubscription zkSubscription = new ZKSubscription();
            zkSubscription.setTopic(topic);

            // local cache
            this.topics.get(MQTT_IN_BOUND).put(zkSubscription.getTopic(), zkSubscription);

            //update zk
            return zkAdd(true, zkSubscription);
        } else {
            log.info("i am not leader, route to master");

            return routeMaster(urlFormat);
        }
    }

    public boolean mqttRemoveInBoundTopic(String topic, String urlFormat) throws BrokerException {
        log.info("remove mqtt inbound topic: {}", topic);

        checkSupport();

        if (this.isMaster) {
            log.info("i am leader, do it directly");

            this.mqttPahoMessageDrivenChannelAdapter.removeTopic(topic);

            // local cache
            this.topics.get(MQTT_IN_BOUND).remove(topic);

            //update zk
            return zkRemove(true, topic);
        } else {
            log.info("i am not leader, route to master");

            return routeMaster(urlFormat);
        }
    }

    public boolean mqttAddOutBoundTopic(String topic, String urlFormat) throws BrokerException {
        log.info("add mqtt outbound topic: {}", topic);

        checkSupport();

        if (this.isMaster) {
            log.info("i am leader, do it directly");

            String subscriptionId = this.mqttBridge.bindOutboundTopic(topic);
            log.info("bind subscriptionId: {}", subscriptionId);

            if (!subscriptionId.isEmpty()) {

                ZKSubscription zkSubscription = new ZKSubscription();
                zkSubscription.setTopic(topic);

                // local cache
                zkSubscription.setSubscriptionId(subscriptionId);
                this.topics.get(MQTT_OUT_BOUND).put(zkSubscription.getTopic(), zkSubscription);

                //update zk
                return zkAdd(false, zkSubscription);
            }

            return false;
        } else {
            log.info("i am not leader, route to master");

            return routeMaster(urlFormat);
        }
    }

    public boolean mqttRemoveOutBoundTopic(String topic, String urlFormat) throws BrokerException {
        log.info("remove mqtt outbound topic: {}", topic);

        checkSupport();

        if (this.isMaster) {
            if (!this.topics.get(MQTT_OUT_BOUND).containsKey(topic)) {
                log.error("not exist topic");
                throw new BrokerException(ErrorCode.TOPIC_NOT_EXIST);
            }

            String subscriptionId = this.topics.get(MQTT_OUT_BOUND).get(topic).getSubscriptionId();
            log.info("find binding subscriptionId: {}", subscriptionId);

            if (!subscriptionId.isEmpty()) {
                this.mqttBridge.unBindOutboundTopic(subscriptionId);

                // local cache
                this.topics.get(MQTT_OUT_BOUND).remove(topic);

                //update zk
                return zkRemove(false, topic);
            }
            return false;
        } else {
            log.info("i am not leader, route to master");

            return routeMaster(urlFormat);
        }
    }

    private boolean routeMaster(String urlFormat) throws BrokerException {
        try {
            SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
            requestFactory.setConnectTimeout(BrokerApplication.weEventConfig.getRestful_timeout());
            requestFactory.setReadTimeout(BrokerApplication.weEventConfig.getRestful_timeout());
            RestTemplate restTemplate = new RestTemplate(requestFactory);

            String masterUrl = String.format(urlFormat, this.masterJob.getMasterAddress());
            log.info("route to master, url: {}", masterUrl);
            ResponseEntity<Boolean> response = restTemplate.getForEntity(masterUrl, Boolean.class);
            log.info("route to master result: {}", response.getBody());
            return response.getBody().booleanValue();
        } catch (Exception e) {
            log.error("route request to master failed", e);
            throw new BrokerException(ErrorCode.HA_ROUTE_TO_MASTER_FAILED);
        }
    }

    private boolean zkAdd(boolean inbound, ZKSubscription zkSubscription) {
        try {
            log.info("add zk topic, inbound: {} topic: {}", inbound, zkSubscription.getTopic());

            // update zookeeper
            this.masterJob.getClient().create().forPath(this.zookeeperPath + (inbound ? "/inbound/" : "/outbound/") + zkSubscription.getTopic(),
                    SerializeUtils.serialize(zkSubscription));
            return true;
        } catch (Exception e) {
            log.error("add topic node failed", e);
            return false;
        }
    }

    private boolean zkRemove(boolean inbound, String topic) {
        try {
            log.info("remove zk topic, inbound: {} topic: {}", inbound, topic);

            // update zookeeper
            this.masterJob.getClient().delete().forPath(this.zookeeperPath + (inbound ? "/inbound/" : "/outbound/") + topic);
            return true;
        } catch (Exception e) {
            log.error("delete topic node failed", e);
            return false;
        }
    }

    @Override
    public String toString() {
        return "MqttTopic{" +
                "isMaster=" + this.isMaster +
                ", zookeeperPath='" + this.zookeeperPath + '\'' +
                ", topics=" + this.topics +
                '}';
    }
}
