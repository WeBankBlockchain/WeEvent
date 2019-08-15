package com.webank.weevent.broker.ha;


import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.webank.weevent.BrokerApplication;
import com.webank.weevent.broker.fisco.constant.WeEventConstants;
import com.webank.weevent.broker.fisco.util.ParamCheckUtils;
import com.webank.weevent.broker.fisco.util.SystemInfoUtils;
import com.webank.weevent.broker.plugin.IConsumer;
import com.webank.weevent.protocol.jsonrpc.IBrokerRpcCallback;
import com.webank.weevent.protocol.rest.SubscriptionWeEvent;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.ErrorCode;
import com.webank.weevent.sdk.WeEvent;

import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import com.googlecode.jsonrpc4j.ProxyUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.PathUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;


/**
 * Jobs that running in master node.
 * This class's main job is dealing with switch master mode, and real jobs is depend on MqttTopic.
 *
 * @author matthewliu
 * @since 2019/03/12
 */
@Slf4j
public class MasterJob {
    // zookeeper state
    private CuratorFramework client;
    private String zookeeperRootPath;
    private String leaderPath;
    private boolean isMaster = false;
    private CGISubscription cgiSubscription;
    private IConsumer consumer;

    public MasterJob() {
        // handler ha and master job
        if (!BrokerApplication.weEventConfig.getZookeeperIp().isEmpty() && !BrokerApplication.weEventConfig.getZookeeperPath().isEmpty()) {
            log.info("init zookeeper");

            this.client = initZookeeper(BrokerApplication.weEventConfig.getZookeeperPath());
            if (this.client == null) {
                log.error("init zookeeper failed");
            }
        }
        this.consumer = BrokerApplication.applicationContext.getBean(IConsumer.class);
    }

    public CuratorFramework getClient() {
        return this.client;
    }

    public CGISubscription getCgiSubscription() {
        return this.cgiSubscription;
    }

    private RestTemplate getRestCallback() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(BrokerApplication.weEventConfig.getRestful_timeout());
        requestFactory.setReadTimeout(BrokerApplication.weEventConfig.getRestful_timeout());
        return new RestTemplate(requestFactory);
    }

    public boolean doUnsubscribe(String type, String subscriptionId, String urlFormat) throws BrokerException {
        log.info("json rpc unSubscribe, subscriptionId: {}", subscriptionId);
        if (this.cgiSubscription == null) {
            boolean result = this.consumer.unSubscribe(subscriptionId);
            return result;
        } else {
            if (type.equals(WeEventConstants.JSONRPCTYPE)) {
                return this.getCgiSubscription().jsonRpcUnSubscribe(subscriptionId);
            } else {
                return this.getCgiSubscription().restUnsubscribe(subscriptionId, urlFormat);
            }
        }


    }

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

    public String doSubscribe(String type, String topic, String groupId, String subscriptionId, String url, String urlFormat) throws BrokerException {
        ParamCheckUtils.validateUrl(url);
        RestTemplate restCallback = getRestCallback();
        IBrokerRpcCallback jsonCallback = getJsonRpcCallback(url);
        if (jsonCallback == null) {
            log.error("invalid notify url, {}", url);
            throw new BrokerException(ErrorCode.URL_INVALID_FORMAT);
        }

        // external params
        Map<IConsumer.SubscribeExt, String> ext = new HashMap<>();
        ext.put(IConsumer.SubscribeExt.InterfaceType, type);
        if (!StringUtils.isBlank(subscriptionId)) {
            log.info("subscribe again, subscriptionId: {}", subscriptionId);
            ext.put(IConsumer.SubscribeExt.SubscriptionId, subscriptionId);
        }

        if (this.cgiSubscription == null) {
            IConsumer.ConsumerListener listener;
            if (type.equals(WeEventConstants.RESTFULTYPE)) {
                listener = new IConsumer.ConsumerListener() {
                    @Override
                    public void onEvent(String subscriptionId, WeEvent event) {
                        try {
                            SubscriptionWeEvent subscriptionWeEvent = new SubscriptionWeEvent();
                            subscriptionWeEvent.setSubscriptionId(subscriptionId);
                            subscriptionWeEvent.setEvent(event);
                            ResponseEntity<Void> response = restCallback.postForEntity(url, subscriptionWeEvent, Void.class);
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
            } else {
                listener = new IConsumer.ConsumerListener() {
                    @Override
                    public void onEvent(String subscriptionId, WeEvent event) {
                        try {
                            jsonCallback.onEvent(subscriptionId, event);
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
            }

            return this.consumer.subscribe(topic, groupId, WeEvent.OFFSET_LAST, ext, listener);
        } else {
            if (type.equals(WeEventConstants.RESTFULTYPE)) {
                return this.getCgiSubscription().restSubscribe(topic,
                        groupId,
                        subscriptionId,
                        url,
                        urlFormat);

            } else {
                return this.getCgiSubscription().jsonRpcSubscribe(topic, groupId, subscriptionId, url);
            }
        }

    }


    // create path/node if not exist
    private boolean ensurePath(CuratorFramework client, String... paths) {
        try {
            for (String path : paths) {
                log.info("start to ensure zookeeper path exist, {}", path);

                client.checkExists().creatingParentsIfNeeded().forPath(path);
                // return null if not exist
                if (client.checkExists().forPath(path) == null) {
                    client.create().forPath(path);
                    log.info("not exist node create now, {}", path);
                }
            }
            return true;
        } catch (Exception e) {
            log.error("ensure zookeeper path failed", e);
            return false;
        }
    }

    private void setMasterAddress() {
        try {
            String localIP = SystemInfoUtils.getCurrentIp();
            String address = localIP + ":" + BrokerApplication.environment.getProperty("server.port");
            log.info("set master address in zookeeper, {}={}", this.leaderPath, address);
            client.setData().forPath(this.leaderPath, address.getBytes());
        } catch (Exception e) {
            log.error("set master address in zookeeper failed", e);
        }
    }

    public String getMasterAddress() {
        try {
            String address = new String(getZookeeperNode(this.leaderPath));
            log.info("get master address in zookeeper, {} = {}", this.leaderPath, address);
            return address;
        } catch (Exception e) {
            log.error("get master address in zookeeper failed", e);
            return "";
        }
    }

    public byte[] getZookeeperNode(String node) {
        try {
            Stat stat = new Stat();
            byte[] data = this.client.getData().storingStatIn(stat).forPath(node);
            log.info("get node data in zookeeper, node: {} data size: {}", this.leaderPath, data.length);
            return data;
        } catch (Exception e) {
            log.error("get node data in zookeeper failed", e);
            return null;
        }
    }

    private boolean writeNodes(CuratorFramework client, CreateMode createMode, String nodesPath, String nodeName) {
        try {
            if (client.checkExists().forPath(nodesPath + "/" + nodeName) == null) {
                client.create().withMode(createMode).forPath(nodesPath + "/" + nodeName, nodeName.getBytes());
            }
            return true;
        } catch (Exception e) {
            log.error("upload node ip and port to zookeeper error:{}", e.getMessage());
        }
        return false;
    }

    private CuratorFramework initZookeeper(String path) {
        try {
            PathUtils.validatePath(path);
        } catch (IllegalArgumentException e) {
            log.error("invalid zookeeper path", e);
            return null;
        }

        this.zookeeperRootPath = path;
        log.info("connect to zookeeper: {}", BrokerApplication.weEventConfig.getZookeeperIp());
        //nodes ip and port config
        String nodesPath = this.zookeeperRootPath + "/nodes";
        String nodeName = SystemInfoUtils.getCurrentIp() + ":" + SystemInfoUtils.getCurrentPort();

        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        CuratorFramework client = CuratorFrameworkFactory.newClient(
                BrokerApplication.weEventConfig.getZookeeperIp(),
                BrokerApplication.weEventConfig.getZookeeperTimeout(),
                1000,
                retryPolicy);

        client.getConnectionStateListenable().addListener(new ConnectionStateListener() {
            @Override
            public void stateChanged(CuratorFramework client, ConnectionState newState) {
                log.info("zookeeper connection state changed, {}", newState);
                if (newState == ConnectionState.RECONNECTED) {
                    //when reconnection upload the nodes ip and port to zookeeper
                    if (writeNodes(client, CreateMode.EPHEMERAL, nodesPath, nodeName)) {
                        log.info("reconnected writer nodes to zookeeper success");
                    }
                }
            }
        });
        //CloseableUtils.closeQuietly(client)
        client.start();

        // cache path "/event-broker/mqtt-topic"
        String mqttTopicPath = this.zookeeperRootPath + "/mqtt_topic";
        String cgiSubscriptionPath = this.zookeeperRootPath + "/cgi_subscription";
        log.info("mqtt topic path: {} cgi subscription path: {} nodes path:{}", mqttTopicPath, cgiSubscriptionPath, nodesPath);
        if (!ensurePath(client, mqttTopicPath + "/inbound", mqttTopicPath + "/outbound", cgiSubscriptionPath, nodesPath)) {
            return null;
        }

        //when connection upload the nodes ip and port to zookeeper
        if (!writeNodes(client, CreateMode.EPHEMERAL, nodesPath, nodeName)) {
            return null;
        }

        this.cgiSubscription = new CGISubscription(this, cgiSubscriptionPath);

        // leader path "/event-broker/master"
        this.leaderPath = this.zookeeperRootPath + "/master";
        log.info("start to select leader, {}", leaderPath);
        // done after 20 seconds
        LeaderSelector selector = new LeaderSelector(client, leaderPath,
                new LeaderSelectorListenerAdapter() {
                    @Override
                    public void takeLeadership(CuratorFramework client) throws Exception {
                        log.info("i am leader now, start jobs");

                        isMaster = true;
                        // record master address
                        setMasterAddress();
                        startJob();

                        // lead until ConnectionState changed
                        while (isMaster) {
                            Thread.sleep(10000);
                            log.info("i am leader");
                            showJob();
                        }

                        // takeLeadership exit meanings relinquish
                    }

                    @Override
                    public void stateChanged(CuratorFramework client, ConnectionState newState) {
                        log.info("zookeeper connection state changed, {}", newState);

                        if (!newState.isConnected()) {
                            log.info("i am not leader now, stop jobs");
                            isMaster = false;
                            stopJob();
                        }

                        // this method will throw CancelLeadershipException, and then interrupt takeLeadership to exit
                        super.stateChanged(client, newState);
                    }
                });
        // can relinquish leader again if lost
        selector.autoRequeue();
        //selector.close();
        selector.start();

        log.info("init zookeeper done");
        return client;
    }

    private void showJob() {
        log.info("cgi subscription: {}", cgiSubscription);
    }

    private void startJob() {
        log.info("try to start master job");

        boolean result = this.cgiSubscription.switchMode(true);
        if (!result) {
            log.error("CGISubscription switch to master mode failed");
            return;
        }
    }

    private void stopJob() {
        log.info("try to stop master job");

        boolean result = this.cgiSubscription.switchMode(false);
        if (!result) {
            log.error("CGISubscription switch to slave mode failed");
            return;
        }
    }
}
