package com.webank.weevent.broker.fisco;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

import com.webank.weevent.BrokerApplication;
import com.webank.weevent.broker.fisco.dto.SubscriptionInfo;
import com.webank.weevent.broker.fisco.util.ParamCheckUtils;
import com.webank.weevent.broker.fisco.web3sdk.FiscoBcosDelegate;
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

/**
 * Event broker's consumer implement in FISCO-BCOS.
 * This class is thread safe.
 *
 * @author matthewliu
 * @since 2018/11/02
 */
@Slf4j
public class FiscoBcosBroker4Consumer extends FiscoBcosTopicAdmin implements IConsumer, IBlockChain, FiscoBcosDelegate.IBlockEventListener {
    /**
     * Subscription ID <-> Subscription
     */
    private Map<String, Subscription> subscriptions = new ConcurrentHashMap<>();

    /**
     * Group ID <-> MainEventLoop
     */
    private Map<Long, MainEventLoop> mainEventLoops = new ConcurrentHashMap<>();

    /**
     * daemon executor
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

    public FiscoBcosBroker4Consumer(FiscoBcosDelegate fiscoBcosDelegate) {
        super(fiscoBcosDelegate);

        // spring default Executor
        this.executor = BrokerApplication.applicationContext.getBean("taskExecutor", Executor.class);
        this.idleTime = fiscoBcosDelegate.getFiscoConfig().getConsumerIdleTime();
        fiscoBcosDelegate.setListener(this);
    }

    private static boolean isEventId(String offset) {
        return !WeEvent.OFFSET_FIRST.equals(offset) && !WeEvent.OFFSET_LAST.equals(offset);
    }

    // topic may be a topic pattern
    @Override
    public String subscribe(String topic, String groupIdStr, String offset,
                            @NonNull Map<SubscribeExt, String> ext,
                            @NonNull ConsumerListener listener) throws BrokerException {
        String groupId = selectGroupId(groupIdStr);
        this.validateGroupId(groupId);
        ParamCheckUtils.validateOffset(offset);

        // topic pattern
        if (Subscription.isTopicPattern(topic)) {
            Subscription.validateTopicPattern(topic);
            if (isEventId(offset)) {
                // not a topic name
                ParamCheckUtils.validateEventId("", offset, fiscoBcosDelegate.getBlockHeight(Long.parseLong(groupId)));
            }
        } else {    // topic name
            ParamCheckUtils.validateTopicName(topic);

            // check topic exist
            if (!this.exist(topic, groupId)) {
                throw new BrokerException(ErrorCode.TOPIC_NOT_EXIST);
            }

            if (isEventId(offset)) {
                ParamCheckUtils.validateEventId(topic, offset, fiscoBcosDelegate.getBlockHeight(Long.parseLong(groupId)));
            }
        }

        log.info("subscribe groupId: {} topic: {} offset: {} ext: {}", groupId, topic, offset, ext);
        return subscribeTopic(topic, Long.valueOf(groupId), offset, ext, listener);
    }

    @Override
    public String subscribe(String[] topics, String groupIdStr, String offset,
                            @NonNull Map<SubscribeExt, String> ext,
                            @NonNull ConsumerListener listener) throws BrokerException {
        // check params
        if (topics == null || topics.length == 0) {
            throw new BrokerException(ErrorCode.TOPIC_LIST_IS_NULL);
        }

        String groupId = selectGroupId(groupIdStr);
        this.validateGroupId(groupId);
        ParamCheckUtils.validateOffset(offset);

        if (isEventId(offset)) {
            // do not validate topic name and eventId if more then one topic
            ParamCheckUtils.validateEventId(topics.length > 1 ? "" : topics[0],
                    offset,
                    fiscoBcosDelegate.getBlockHeight(Long.parseLong(groupId)));
        }

        for (String topic : topics) {
            if (Subscription.isTopicPattern(topic)) {
                Subscription.validateTopicPattern(topic);
            } else {
                ParamCheckUtils.validateTopicName(topic);

                // check topic exist
                if (!this.exist(topic, groupId)) {
                    throw new BrokerException(ErrorCode.TOPIC_NOT_EXIST);
                }
            }
        }

        log.info("subscribe groupId:{} topics: {} offset: {} ext:{}", groupId, Arrays.toString(topics), offset, ext);
        return subscribeTopic(topics, Long.valueOf(groupId), offset, ext, listener);
    }

    private String subscribeTopic(String topic, Long groupId, String offset, Map<SubscribeExt, String> ext, ConsumerListener listener) throws BrokerException {
        String[] topics = {topic};
        return subscribeTopic(topics, groupId, offset, ext, listener);
    }

    private String subscribeTopic(String[] topics, Long groupId, String offset, Map<SubscribeExt, String> ext, ConsumerListener listener) throws BrokerException {
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
                String.valueOf(groupId),
                offset,
                tag,
                listener);
        subscription.setMergeBlock(fiscoBcosDelegate.getFiscoConfig().getConsumerHistoryMergeBlock());
        subscription.setInterfaceType(interfaceType);
        subscription.setRemoteIp(remoteIp);

        this.subscriptions.put(subscription.getUuid(), subscription);
        this.mainEventLoops.get(groupId).addSubscription(subscription);

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
        this.mainEventLoops.get(Long.valueOf(subscription.getGroupId())).removeSubscription(subscription);
        this.subscriptions.remove(subscriptionId);

        log.info("unSubscribe success, subscriptionId {}", subscriptionId);
        return true;
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
        for (String groupId : fiscoBcosDelegate.listGroupId()) {
            MainEventLoop mainEventLoop = new MainEventLoop(this.executor, this, groupId);
            mainEventLoop.doStart();
            Long gid = Long.valueOf(groupId);
            this.mainEventLoops.put(gid, mainEventLoop);
        }

        this.consumerStarted = true;
        log.info("start consumer finish");
        return true;
    }

    @Override
    public synchronized boolean shutdownConsumer() {
        // stop main event loop and referred subscription
        for (Map.Entry<Long, MainEventLoop> mainEventLoop : this.mainEventLoops.entrySet()) {
            mainEventLoop.getValue().doStop();
        }
        this.mainEventLoops.clear();
        this.subscriptions.clear();

        this.consumerStarted = false;

        log.info("shutdown consumer finish");
        return true;
    }

    @Override
    public synchronized Map<String, Object> listSubscription(String groupIdStr) throws BrokerException {
        String groupId = selectGroupId(groupIdStr);
        this.validateGroupId(groupId);
        Map<String, Object> subscribeIdList = new HashMap<>();
        for (Map.Entry<String, Subscription> entry : this.subscriptions.entrySet()) {
            Subscription subscription = entry.getValue();
            if (!groupId.equals(subscription.getGroupId())) {
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

    // methods from IBlockChain
    @Override
    public int getIdleTime() {
        return this.idleTime;
    }

    @Override
    public Long getBlockHeight(String groupId) throws BrokerException {
        return fiscoBcosDelegate.getBlockHeight(Long.valueOf(groupId));
    }

    @Override
    public boolean hasBlockEventNotify() {
        return fiscoBcosDelegate.supportBlockEventNotify();
    }

    @Override
    public List<WeEvent> loop(Long blockNum, String groupId) throws BrokerException {
        return fiscoBcosDelegate.loop(blockNum, Long.valueOf(groupId));
    }

    // method from FiscoBcosDelegate.IBlockEventListener
    @Override
    public void onEvent(Long groupId, Long blockHeight) {
        if (this.mainEventLoops.containsKey(groupId)) {
            this.mainEventLoops.get(groupId).onNewBlock(blockHeight);
        }
    }
}
