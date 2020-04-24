package com.webank.weevent.core.fabric;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

import com.webank.weevent.client.BrokerException;
import com.webank.weevent.client.ErrorCode;
import com.webank.weevent.client.WeEvent;
import com.webank.weevent.core.IConsumer;
import com.webank.weevent.core.dto.SubscriptionInfo;
import com.webank.weevent.core.fabric.sdk.FabricDelegate;
import com.webank.weevent.core.fisco.util.ParamCheckUtils;
import com.webank.weevent.core.task.IBlockChain;
import com.webank.weevent.core.task.MainEventLoop;
import com.webank.weevent.core.task.Subscription;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * @author websterchen
 * @version v1.1
 * @since 2019/8/28
 */
@Slf4j
public class FabricBroker4Consumer extends FabricTopicAdmin implements IConsumer, IBlockChain {
    /**
     * Subscription ID <-> Subscription
     */
    private Map<String, Subscription> subscriptions = new ConcurrentHashMap<>();

    /**
     * channelName <-> MainEventLoop
     */
    private Map<String, MainEventLoop> mainEventLoops = new ConcurrentHashMap<>();

    /**
     * daemon Executor
     */
    private Executor executor;

    /**
     * Whether the Consumer has started
     */
    private boolean consumerStarted = false;

    /**
     * idle time if no new block
     */
    private int idleTime;

    public FabricBroker4Consumer(FabricDelegate fabricDelegate) {
        super(fabricDelegate);

        this.executor = fabricDelegate.getThreadPool();
        this.idleTime = fabricDelegate.getFabricConfig().getConsumerIdleTime();
    }

    private static boolean isEventId(String offset) {
        return !WeEvent.OFFSET_FIRST.equals(offset) && !WeEvent.OFFSET_LAST.equals(offset);
    }

    // topic may be a topic pattern
    @Override
    public String subscribe(String topic, String channelName, String offset,
                            @NonNull Map<SubscribeExt, String> ext,
                            @NonNull ConsumerListener listener) throws BrokerException {
        this.validateChannelName(channelName);
        ParamCheckUtils.validateOffset(offset);

        Long currentBlock = fabricDelegate.getBlockHeight(channelName);
        // topic pattern
        if (Subscription.isTopicPattern(topic)) {
            Subscription.validateTopicPattern(topic);
            if (StringUtils.isNumeric(offset)) {
                ParamCheckUtils.validateBlockHeight(offset, currentBlock);
            } else if (isEventId(offset)) {
                // not a topic name
                ParamCheckUtils.validateEventId("", offset, currentBlock);
            }
        } else {    // topic name
            ParamCheckUtils.validateTopicName(topic);

            // check topic exist
            if (!this.exist(topic, channelName)) {
                throw new BrokerException(ErrorCode.TOPIC_NOT_EXIST);
            }

            if (StringUtils.isNumeric(offset)) {
                ParamCheckUtils.validateBlockHeight(offset, currentBlock);
            } else if (isEventId(offset)) {
                ParamCheckUtils.validateEventId(topic, offset, currentBlock);
            }
        }

        log.info("subscribe channelName: {} topic: {} offset: {} ext: {}", channelName, topic, offset, ext);
        return subscribeTopic(topic, channelName, offset, ext, listener);
    }

    @Override
    public String subscribe(String[] topics, String channelName, String offset,
                            @NonNull Map<SubscribeExt, String> ext,
                            @NonNull ConsumerListener listener) throws BrokerException {
        // check params
        if (topics == null || topics.length == 0) {
            throw new BrokerException(ErrorCode.TOPIC_LIST_IS_NULL);
        }

        this.validateChannelName(channelName);
        ParamCheckUtils.validateOffset(offset);

        Long currentBlock = fabricDelegate.getBlockHeight(channelName);
        if (StringUtils.isNumeric(offset)) {
            ParamCheckUtils.validateBlockHeight(offset, currentBlock);
        } else if (isEventId(offset)) {
            // do not validate topic name and eventId if more then one topic
            ParamCheckUtils.validateEventId(topics.length > 1 ? "" : topics[0], offset, currentBlock);
        }

        for (String topic : topics) {
            if (Subscription.isTopicPattern(topic)) {
                Subscription.validateTopicPattern(topic);
            } else {
                ParamCheckUtils.validateTopicName(topic);
                // check topic exist
                if (!this.exist(topic, channelName)) {
                    throw new BrokerException(ErrorCode.TOPIC_NOT_EXIST);
                }
            }
        }

        log.info("subscribe channelName:{} topics: {} offset: {} ext:{}", channelName, Arrays.toString(topics), offset, ext);
        return subscribeTopic(topics, channelName, offset, ext, listener);
    }

    private String subscribeTopic(String topic, String channelName, String offset, Map<SubscribeExt, String> ext,
                                  ConsumerListener listener) throws BrokerException {
        String[] topics = {topic};
        return subscribeTopic(topics, channelName, offset, ext, listener);
    }

    private String subscribeTopic(String[] topics, String channelName, String offset, Map<SubscribeExt, String> ext,
                                  ConsumerListener listener) throws BrokerException {
        // external params
        String interfaceType = "";
        if (ext.containsKey(SubscribeExt.InterfaceType)) {
            interfaceType = ext.get(SubscribeExt.InterfaceType);
        }
        String remoteIp = "";
        if (ext.containsKey(SubscribeExt.RemoteIP)) {
            remoteIp = ext.get(SubscribeExt.RemoteIP);
        }
        String tag = "";
        if (ext.containsKey(SubscribeExt.TopicTag)) {
            tag = ext.get(SubscribeExt.TopicTag);
            if (StringUtils.isBlank(tag)) {
                throw new BrokerException(ErrorCode.TOPIC_TAG_IS_BLANK);
            }
        }

        // custom input subscriptionId, support in STOMP
        String subscriptionId = "";
        if (ext.containsKey(SubscribeExt.SubscriptionId)) {
            subscriptionId = ext.get(SubscribeExt.SubscriptionId);
            ParamCheckUtils.validateSubscriptionId(subscriptionId);
        }

        if (this.subscriptions.containsKey(subscriptionId)) {
            log.info("already exist subscription: {}", subscriptionId);

            // subscription in MQTT and STOMP is connection orientated, will auto unsubscribe when connection lost.
            // so it's something wrong when already exist
            throw new BrokerException(ErrorCode.SUBSCRIPTIONID_ALREADY_EXIST);
        }

        // new subscribe
        Subscription subscription = new Subscription(this,
                subscriptionId,
                topics,
                channelName,
                offset,
                tag,
                listener);
        subscription.setIdleTime(this.idleTime);
        subscription.setMergeBlock(fabricDelegate.getFabricConfig().getConsumerHistoryMergeBlock());
        subscription.setInterfaceType(interfaceType);
        subscription.setRemoteIp(remoteIp);

        this.subscriptions.put(subscription.getUuid(), subscription);
        this.mainEventLoops.get(channelName).addSubscription(subscription);

        return subscription.getUuid();
    }

    @Override
    public boolean unSubscribe(String subscriptionId) throws BrokerException {
        if (StringUtils.isBlank(subscriptionId)) {
            throw new BrokerException(ErrorCode.SUBSCRIPTIONID_IS_BLANK);
        }

        if (!this.subscriptions.containsKey(subscriptionId)) {
            log.warn("not exist subscriptionId {}", subscriptionId);
            throw new BrokerException(ErrorCode.SUBSCRIPTIONID_NOT_EXIST);
        }

        Subscription subscription = this.subscriptions.get(subscriptionId);
        this.mainEventLoops.get(subscription.getGroupId()).removeSubscription(subscription);
        this.subscriptions.remove(subscriptionId);

        log.info("unSubscribe success, subscriptionId {}", subscriptionId);
        return true;
    }

    @Override
    public Map<String, SubscriptionInfo> listSubscription(String channnelName) throws BrokerException {
        this.validateChannelName(channnelName);
        Map<String, SubscriptionInfo> subscribeIdList = new HashMap<>();
        for (Map.Entry<String, Subscription> entry : this.subscriptions.entrySet()) {
            Subscription subscription = entry.getValue();
            if (channnelName.equals(subscription.getGroupId())) {
                SubscriptionInfo subscriptionInfo = SubscriptionInfo.fromSubscription(subscription);
                subscribeIdList.put(subscription.getUuid(), subscriptionInfo);
            }
        }

        log.debug("subscriptions: {}", this.subscriptions.toString());
        return subscribeIdList;
    }

    @Override
    public boolean isStarted() {
        return this.consumerStarted;
    }

    @Override
    public synchronized boolean startConsumer() throws BrokerException {
        if (this.consumerStarted) {
            throw new BrokerException(ErrorCode.CONSUMER_ALREADY_STARTED);
        }

        // load MainEventLoop with configuration
        for (String channelName : fabricDelegate.listChannel()) {
            MainEventLoop mainEventLoop = new MainEventLoop(this.executor, this, channelName);
            mainEventLoop.doStart();
            this.mainEventLoops.put(channelName, mainEventLoop);
        }

        this.consumerStarted = true;
        log.info("start consumer finish");
        return true;
    }

    @Override
    public synchronized boolean shutdownConsumer() {
        // stop main event loop and referred subscription
        for (Map.Entry<String, MainEventLoop> mainEventLoop : this.mainEventLoops.entrySet()) {
            mainEventLoop.getValue().doStop();
        }
        this.mainEventLoops.clear();
        this.subscriptions.clear();

        this.consumerStarted = false;

        log.info("shutdown consumer finish");
        return true;
    }

    @Override
    public int getIdleTime() {
        return this.idleTime;
    }

    @Override
    public Long getBlockHeight(String channelName) throws BrokerException {
        return fabricDelegate.getBlockHeight(channelName);
    }

    @Override
    public boolean hasBlockEventNotify() {
        return false;
    }

    @Override
    public List<WeEvent> loop(Long blockNum, String channelName) {
        return fabricDelegate.loop(blockNum, channelName);
    }
}
