package com.webank.weevent.broker.ha;


import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.webank.weevent.BrokerApplication;
import com.webank.weevent.broker.fisco.util.ParamCheckUtils;
import com.webank.weevent.broker.fisco.util.SerializeUtils;
import com.webank.weevent.broker.plugin.IConsumer;
import com.webank.weevent.protocol.rest.SubscriptionWeEvent;
import com.webank.weevent.protocol.jsonrpc.IBrokerRpcCallback;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.ErrorCode;
import com.webank.weevent.sdk.WeEvent;
import com.webank.weevent.sdk.jsonrpc.IBrokerRpc;

import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import com.googlecode.jsonrpc4j.ProxyUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * CGI subscription in local cache and zookeeper.
 *
 * @author matthewliu
 * @since 2019/03/13
 */
@Slf4j
public class CGISubscription {
    // consumer handler
    private IConsumer consumer;

    // zookeeper state
    private MasterJob masterJob;
    private boolean isMaster;
    private String zookeeperPath;
    // subscription id <-> subscription
    private Map<String, ZKSubscription> topics;

    CGISubscription(MasterJob masterJob, String path) {
        this.masterJob = masterJob;
        // default in slave mode
        this.isMaster = false;
        this.zookeeperPath = path;

        this.consumer = BrokerApplication.applicationContext.getBean(IConsumer.class);
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
                nodes.put(zkSubscription.getSubscriptionId(), zkSubscription);
            }
            return nodes;
        } catch (Exception e) {
            log.error("flush node failed", e);
            return null;
        }
    }

    public boolean switchMode(boolean master) {
        this.isMaster = master;
        if (!this.isMaster) {
            log.info("switch in slave mode");
            for (Map.Entry<String, ZKSubscription> topics : this.topics.entrySet()) {
                try {
                    this.consumer.unSubscribe(topics.getKey());
                } catch (Exception e) {
                    log.error("unSubscribe {} error,topicName:{}", topics.getKey(), topics.getValue().getTopic());
                }
            }
            this.topics.clear();
            return true;
        }

        // if in master mode, flush subscriptions from zookeeper into memory
        log.info("switch in master mode");
        this.topics = flushNode(this.zookeeperPath);
        if (this.topics == null) {
            return false;
        }

        // active subscription in zookeeper
        for (Map.Entry<String, ZKSubscription> subscription : this.topics.entrySet()) {
            ZKSubscription zkSubscription = subscription.getValue();
            try {
                if (zkSubscription.getRestful()) {
                    doRestSubscribe(zkSubscription.getTopic(), zkSubscription.getSubscriptionId(), zkSubscription.getCallbackUrl());
                } else {
                    doJsonRpcSubscribe(zkSubscription.getTopic(), zkSubscription.getSubscriptionId(), zkSubscription.getCallbackUrl());
                }
            } catch (BrokerException e) {
                log.error("subscribe from zookeeper failed", e);
            }
        }

        return true;
    }

    private void checkSupport() throws BrokerException {
        if (this.masterJob.getClient() == null) {
            log.error("no broker.zookeeper.ip configuration, skip it");
            throw new BrokerException(ErrorCode.CGI_SUBSCRIPTION_NO_ZOOKEEPER);
        }
    }

    /**
     * notify handler
     *
     * @param url remote notify endian
     * @return notify handler, null if error
     */
    private IBrokerRpcCallback getJsonRpcCallback(String url) {
        try {
            JsonRpcHttpClient client = new JsonRpcHttpClient(new URL(url));
            // check url format only, do no check whether it can be accessed
            return ProxyUtil.createClientProxy(client.getClass().getClassLoader(), IBrokerRpcCallback.class, client);
        } catch (MalformedURLException e) {
            log.error("getCallback handler exception", e);
            return null;
        }
    }

    private IBrokerRpc getJsonRpcProxy() {
        try {
            boolean https = BrokerApplication.environment.getProperty("server.ssl.enabled").equals("true");
            String contextPath = BrokerApplication.environment.getProperty("server.servlet.context-path");
            String url = (https ? "https://" : "http://") + this.masterJob.getMasterAddress() + contextPath + "/jsonrpc";

            log.info("route to master, url: {}", url);
            JsonRpcHttpClient client = new JsonRpcHttpClient(new URL(url));
            // check url format only, do no check whether it can be accessed
            return ProxyUtil.createClientProxy(client.getClass().getClassLoader(), IBrokerRpc.class, client);
        } catch (MalformedURLException e) {
            log.error("getCallback handler exception", e);
            return null;
        }
    }

    private ZKSubscription doJsonRpcSubscribe(String topic, String subscriptionId, String url) throws BrokerException {
        IBrokerRpcCallback callback = getJsonRpcCallback(url);
        if (callback == null) {
            log.error("invalid notify url, {}", url);
            throw new BrokerException(ErrorCode.URL_INVALID_FORMAT);
        }

        IConsumer.ConsumerListener listener = new IConsumer.ConsumerListener() {
            @Override
            public void onEvent(String subscriptionId, WeEvent event) {
                try {
                    callback.onEvent(subscriptionId, event);
                    log.info("subscribe callback notify, url: {} subscriptionId: {} event: {}",
                            url, subscriptionId, event);
                } catch (Exception e) {
                    log.error(String.format("subscribe callback notify failed, url: %s subscriptionId: %s",
                            url, subscriptionId), e);
                }
            }

            @Override
            public void onException(Throwable e) {
                log.error("subscribe failed", e);
            }
        };
        String subId;
        if (StringUtils.isBlank(subscriptionId)) {
            log.info("new subscribe, topic: {}", topic);
            subId = this.consumer.subscribe(topic, WeEvent.OFFSET_LAST, "jsonrpc", listener);
        } else {
            log.info("subscribe again, subscriptionId: {}", subscriptionId);
            subId = this.consumer.subscribe(topic, WeEvent.OFFSET_LAST, subscriptionId, "jsonrpc", listener);
        }

        ZKSubscription zkSubscription = new ZKSubscription();
        zkSubscription.setTopic(topic);
        zkSubscription.setSubscriptionId(subId);
        zkSubscription.setRestful(false);
        zkSubscription.setCallbackUrl(url);

        return zkSubscription;
    }

    public String jsonRpcSubscribe(String topic, String subscriptionId, String url) throws BrokerException {
        log.info("json rpc subscribe topic: {}, subscriptionId: {}, url: {}", topic, subscriptionId, url);

        if (this.isMaster) {
            log.info("i am leader, do it directly");

            ParamCheckUtils.validateUrl(url);
            ZKSubscription zkSubscription = doJsonRpcSubscribe(topic, subscriptionId, url);

            // local cache
            this.topics.put(zkSubscription.getSubscriptionId(), zkSubscription);

            //update zk
            zkAdd(zkSubscription);

            return zkSubscription.getSubscriptionId();
        } else {
            log.info("i am not leader, route to master");

            IBrokerRpc brokerRpc = getJsonRpcProxy();
            if (brokerRpc == null) {
                log.error("route request to master failed");
                throw new BrokerException(ErrorCode.HA_ROUTE_TO_MASTER_FAILED);
            }

            return brokerRpc.subscribe(topic, subscriptionId, url);
        }
    }

    public boolean jsonRpcUnSubscribe(String subscriptionId) throws BrokerException {
        log.info("json rpc unSubscribe, subscriptionId: {}", subscriptionId);

        checkSupport();

        if (this.isMaster) {
            log.info("i am leader, do it directly");

            boolean result = this.consumer.unSubscribe(subscriptionId);
            log.info("unSubscribe result: {}", result);

            if (result) {
                // local cache
                this.topics.remove(subscriptionId);

                //update zk
                return zkRemove(subscriptionId);
            }
            return result;
        } else {
            log.info("i am not leader, route to master");

            IBrokerRpc brokerRpc = getJsonRpcProxy();
            if (brokerRpc == null) {
                log.error("route request to master failed");
                throw new BrokerException(ErrorCode.HA_ROUTE_TO_MASTER_FAILED);
            }

            return brokerRpc.unSubscribe(subscriptionId);
        }
    }

    private RestTemplate getRestCallback() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(BrokerApplication.weEventConfig.getRestful_timeout());
        requestFactory.setReadTimeout(BrokerApplication.weEventConfig.getRestful_timeout());
        return new RestTemplate(requestFactory);
    }

    private ZKSubscription doRestSubscribe(String topic, String subscriptionId, String url) throws BrokerException {
        RestTemplate callback = getRestCallback();

        IConsumer.ConsumerListener listener = new IConsumer.ConsumerListener() {
            @Override
            public void onEvent(String subscriptionId, WeEvent event) {
                try {
                    SubscriptionWeEvent subscriptionWeEvent = new SubscriptionWeEvent();
                    subscriptionWeEvent.setSubscriptionId(subscriptionId);
                    subscriptionWeEvent.setEvent(event);
                    ResponseEntity<Void> response = callback.postForEntity(url, subscriptionWeEvent, Void.class);
                    log.info("subscribe callback notify, url: {} subscriptionId: {} event: {} status code: {}",
                            url, subscriptionId, event, response.getStatusCode());
                } catch (Exception e) {
                    log.error(String.format("subscribe callback notify failed, url: %s subscriptionId: %s",
                            url, subscriptionId), e);
                }
            }

            @Override
            public void onException(Throwable e) {
                log.error("subscribe notify failed", e);
            }
        };

        String subId;
        if (StringUtils.isBlank(subscriptionId)) {
            log.info("new subscribe, topic: {}", topic);
            subId = this.consumer.subscribe(topic, WeEvent.OFFSET_LAST, "restful", listener);
        } else {
            log.info("subscribe again, subscriptionId: {}", subscriptionId);
            subId = this.consumer.subscribe(topic, WeEvent.OFFSET_LAST, subscriptionId, "restful", listener);
        }

        ZKSubscription zkSubscription = new ZKSubscription();
        zkSubscription.setTopic(topic);
        zkSubscription.setSubscriptionId(subId);
        zkSubscription.setRestful(true);
        zkSubscription.setCallbackUrl(url);
        return zkSubscription;
    }

    public String restSubscribe(String topic, String subscriptionId, String url, String urlFormat) throws BrokerException {
        log.info("subscribe topic: {}, url: {} subscriptionId:{}", topic, url, subscriptionId);

        if (this.isMaster) {
            log.info("i am leader, do it directly");

            ParamCheckUtils.validateUrl(url);
            ZKSubscription zkSubscription = doRestSubscribe(topic, subscriptionId, url);

            // local cache
            this.topics.put(zkSubscription.getSubscriptionId(), zkSubscription);

            //update zk
            zkAdd(zkSubscription);

            return zkSubscription.getSubscriptionId();
        } else {
            log.info("i am not leader, route to master");

            return routeRestMaster(urlFormat, String.class);
        }
    }

    public boolean restUnsubscribe(String subscriptionId, String urlFormat) throws BrokerException {
        log.info("remove unsubscribe, subscriptionId: {}", subscriptionId);

        if (this.isMaster) {
            log.info("i am leader, do it directly");

            this.consumer.unSubscribe(subscriptionId);


            if (this.topics.containsKey(subscriptionId)) {
                ZKSubscription zkSubscription = this.topics.get(subscriptionId);
                // local cache
                this.topics.remove(subscriptionId);

                //update zk
                return zkRemove(zkSubscription.getSubscriptionId());
            }

            return true;
        } else {
            log.info("i am not leader, route to master");

            return routeRestMaster(urlFormat, boolean.class);
        }
    }

    private <T> T routeRestMaster(String urlFormat, Class<T> responseType) throws BrokerException {
        try {
            SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
            requestFactory.setConnectTimeout(BrokerApplication.weEventConfig.getRestful_timeout());
            requestFactory.setReadTimeout(BrokerApplication.weEventConfig.getRestful_timeout());
            RestTemplate restTemplate = new RestTemplate(requestFactory);

            String masterUrl = String.format(urlFormat, this.masterJob.getMasterAddress());
            log.info("route to master, url: {}", masterUrl);
            ResponseEntity<T> response = restTemplate.getForEntity(masterUrl, responseType);
            log.info("route to master result: {}", response.getBody());
            return response.getBody();
        } catch (Exception e) {
            log.error("route request to master failed", e);
            throw new BrokerException(ErrorCode.HA_ROUTE_TO_MASTER_FAILED);
        }
    }

    private boolean zkAdd(ZKSubscription zkSubscription) {
        try {
            log.info("add zk topic, topic: {}", zkSubscription.getTopic());

            // update zookeeper
            this.masterJob.getClient().create().forPath(this.zookeeperPath + "/" + zkSubscription.getSubscriptionId(),
                    SerializeUtils.serialize(zkSubscription));
            return true;
        } catch (Exception e) {
            log.error("add topic node failed", e);
            return false;
        }
    }

    private boolean zkRemove(String subscriptionId) {
        try {
            log.info("remove zk topic, subscriptionId: {}", subscriptionId);

            // update zookeeper
            this.masterJob.getClient().delete().forPath(this.zookeeperPath + "/" + subscriptionId);
            return true;
        } catch (Exception e) {
            log.error("delete topic node failed", e);
            return false;
        }
    }

    @Override
    public String toString() {
        return "CGISubscription{" +
                "isMaster=" + this.isMaster +
                ", zookeeperPath='" + this.zookeeperPath + '\'' +
                ", topics=" + this.topics +
                '}';
    }
}
