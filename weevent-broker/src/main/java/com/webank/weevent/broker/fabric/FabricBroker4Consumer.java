package com.webank.weevent.broker.fabric;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.webank.weevent.BrokerApplication;
import com.webank.weevent.broker.fisco.dto.SubscriptionInfo;
import com.webank.weevent.broker.fisco.util.ParamCheckUtils;
import com.webank.weevent.broker.plugin.IConsumer;
import com.webank.weevent.broker.task.IBlockChain;
import com.webank.weevent.broker.task.MainEventLoop;
import com.webank.weevent.broker.task.Subscription;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.ErrorCode;
import com.webank.weevent.sdk.WeEvent;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
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
     * daemon thread pool
     */
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    /**
     * Whether the Consumer has started
     */
    private boolean consumerStarted = false;

    /**
     * idle time if no new block
     */
    private int idleTime;

    public FabricBroker4Consumer() {
        super();
        this.threadPoolTaskExecutor = (ThreadPoolTaskExecutor) BrokerApplication.applicationContext.getBean("weevent_daemon_task_executor");
        this.idleTime = fabricConfig.getConsumerIdleTime();
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
        // topic pattern
        if (Subscription.isTopicPattern(topic)) {
            Subscription.validateTopicPattern(topic);
            if (isEventId(offset)) {
                // not a topic name
                ParamCheckUtils.validateEventId("", offset, fabricDelegate.getBlockHeight(channelName));
            }
        } else {    // topic name
            ParamCheckUtils.validateTopicName(topic);

            // check topic exist
            if (!this.exist(topic, channelName)) {
                throw new BrokerException(ErrorCode.TOPIC_NOT_EXIST);
            }

            if (isEventId(offset)) {
                ParamCheckUtils.validateEventId(topic, offset, fabricDelegate.getBlockHeight(channelName));
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

        if (isEventId(offset)) {
            // do not validate topic name and eventId if more then one topic
            ParamCheckUtils.validateEventId(topics.length > 1 ? "" : topics[0],
                    offset,
                    fabricDelegate.getBlockHeight(channelName));
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
        subscription.setMergeBlock(fabricConfig.getConsumerHistoryMergeBlock());
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
    public Map<String, Object> listSubscription(String channnelName) throws BrokerException {
        this.validateChannelName(channnelName);
        Map<String, Object> subscribeIdList = new HashMap<>();
        for (Map.Entry<String, Subscription> entry : this.subscriptions.entrySet()) {
            Subscription subscription = entry.getValue();
            if (!channnelName.equals(subscription.getGroupId())){
                continue;
            }

            SubscriptionInfo subscriptionInfo = new SubscriptionInfo();
            subscriptionInfo.setInterfaceType(subscription.getInterfaceType());
            subscriptionInfo.setNotifiedEventCount(subscription.getNotifiedEventCount().toString());
            subscriptionInfo.setNotifyingEventCount(subscription.getNotifyingEventCount().toString());
            subscriptionInfo.setNotifyTimeStamp(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                    .format(subscription.getNotifyTimeStamp()));
            subscriptionInfo.setRemoteIp(subscription.getRemoteIp());
            subscriptionInfo.setCreateTimeStamp(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                    .format(subscription.getCreateTimeStamp()));
            subscriptionInfo.setGroupId(subscription.getGroupId());

            // Arrays.toString will append plus "[]"
            if (subscription.getTopics().length == 1) {
                subscriptionInfo.setTopicName(subscription.getTopics()[0]);
            } else {
                subscriptionInfo.setTopicName(Arrays.toString(subscription.getTopics()));
            }

            subscriptionInfo.setSubscribeId(subscription.getUuid());
            subscribeIdList.put(subscription.getUuid(), subscriptionInfo);
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
            MainEventLoop mainEventLoop = new MainEventLoop(this.threadPoolTaskExecutor, this, channelName);
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
    public List<WeEvent> loop(Long blockNum, String channelName) throws BrokerException {
        return fabricDelegate.loop(blockNum, channelName);
    }
}
