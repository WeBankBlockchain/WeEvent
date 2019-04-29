package com.webank.weevent.broker.ha;


import com.webank.weevent.BrokerApplication;
import com.webank.weevent.broker.fisco.util.SystemInfoUtils;

import lombok.extern.slf4j.Slf4j;
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
    private MqttTopic mqttTopic;
    private CGISubscription cgiSubscription;

    public MasterJob() {
        // handler ha and master job
        if (!BrokerApplication.weEventConfig.getZookeeperIp().isEmpty() && !BrokerApplication.weEventConfig.getZookeeperPath().isEmpty()) {
            log.info("init zookeeper");

            this.client = initZookeeper(BrokerApplication.weEventConfig.getZookeeperPath());
            if (this.client == null) {
                log.error("init zookeeper failed");
            }
        }
    }

    public CuratorFramework getClient() {
        return this.client;
    }

    public MqttTopic getMqttTopic() {
        return this.mqttTopic;
    }

    public CGISubscription getCgiSubscription() {
        return this.cgiSubscription;
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
        String ip = SystemInfoUtils.getCurrentIp();
        String port = BrokerApplication.weEventConfig.getServerPort();
        String nodeName = ip + ":" + port;

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
                    if (writeNodes(client,CreateMode.EPHEMERAL ,nodesPath,nodeName)) {
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
        if (!writeNodes(client,CreateMode.EPHEMERAL ,nodesPath,nodeName)) {
            return null;
        }

        this.mqttTopic = new MqttTopic(this, mqttTopicPath);
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
        log.info("mqtt topic: {}", mqttTopic);
        log.info("cgi subscription: {}", cgiSubscription);
    }

    private void startJob() {
        log.info("try to start master job");

        boolean result = this.cgiSubscription.switchMode(true);
        if (!result) {
            log.error("CGISubscription switch to master mode failed");
            return;
        }

        result = this.mqttTopic.switchMode(true);
        if (!result) {
            log.error("MqttTopic switch to master mode failed");
        }
    }

    private void stopJob() {
        log.info("try to stop master job");

        boolean result = this.cgiSubscription.switchMode(false);
        if (!result) {
            log.error("CGISubscription switch to slave mode failed");
            return;
        }

        result = this.mqttTopic.switchMode(false);
        if (!result) {
            log.error("MqttTopic switch to slave mode failed");
        }
    }
}
