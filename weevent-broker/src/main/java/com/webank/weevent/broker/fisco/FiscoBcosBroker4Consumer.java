package com.webank.weevent.broker.fisco;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import com.webank.weevent.BrokerApplication;
import com.webank.weevent.broker.fisco.dto.SubscriptionInfo;
import com.webank.weevent.broker.fisco.util.DataTypeUtils;
import com.webank.weevent.broker.fisco.util.ParamCheckUtils;
import com.webank.weevent.broker.fisco.util.StoppableTask;
import com.webank.weevent.broker.fisco.util.WeEventUtils;
import com.webank.weevent.broker.plugin.IConsumer;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.ErrorCode;
import com.webank.weevent.sdk.WeEvent;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Event broker's consumer implement in FISCO-BCOS.
 * This class is thread safe.
 *
 * @author matthewliu
 * @since 2018/11/02
 */
@Slf4j
public class FiscoBcosBroker4Consumer extends FiscoBcosTopicAdmin implements IConsumer {

    /**
     * Subscription ID <-> Subscription
     */
    private Map<String, Subscription> subscriptions = new ConcurrentHashMap<>();

    /**
     * Group ID <-> MainEventLoop
     */
    private Map<Long, MainEventLoop> mainEventLoops = new ConcurrentHashMap<>();

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

    public FiscoBcosBroker4Consumer() {
        super();

        this.threadPoolTaskExecutor = (ThreadPoolTaskExecutor) BrokerApplication.applicationContext.getBean("weevent_daemon_task_executor");
        this.idleTime = fiscoConfig.getConsumerIdleTime();
    }

    /**
     * Idle the caller thread some time
     */
    private void idle() {
        // Transaction commit every 1 second.
        try {
            Thread.sleep(this.idleTime);
        } catch (InterruptedException e) {
            log.warn("got InterruptedException in idle");
        }
    }

    /**
     * filter event with topic name or pattern
     *
     * @param from original event list
     * @param topics topic name or Pattern list
     * @return target event list
     */
    private static List<WeEvent> filter(List<WeEvent> from, String[] topics) {
        List<WeEvent> to = new ArrayList<>();
        for (WeEvent event : from) {
            for (String topic : topics) {
                if (ParamCheckUtils.isTopicPattern(topic)) {
                    if (WeEventUtils.match(event.getTopic(), topic)) {
                        to.add(event);
                    }
                } else {
                    if (topic.equals(event.getTopic())) {
                        to.add(event);
                    }
                }
            }
        }

        return to;
    }

    /**
     * filter event from one block
     *
     * @param blockNum block height
     * @param topics topic list
     * @param groupId groupId
     * @return event list
     * @throws BrokerException the exp
     */
    private List<WeEvent> filterBlockEvent(Long blockNum, String[] topics, Long groupId) throws BrokerException {
        // idle until get event
        List<WeEvent> blockEventsList = null;
        while (blockEventsList == null) {
            blockEventsList = fiscoBcosDelegate.loop(blockNum, groupId);
            if (blockEventsList == null) {
                idle();
            }
        }

        // filter target event
        return filter(blockEventsList, topics);
    }

    @Override
    public String subscribe(String[] topics, String groupId, String offset, String interfaceType, ConsumerListener listener) throws BrokerException {
        // check params
        if (topics == null || topics.length == 0) {
            throw new BrokerException(ErrorCode.TOPIC_LIST_IS_NULL);
        }

        ParamCheckUtils.validateGroupId(groupId);
        ParamCheckUtils.validateOffset(offset);
        if (!offset.equals(WeEvent.OFFSET_FIRST) && !offset.equals(WeEvent.OFFSET_LAST)) {
            // do not validate topic name if more then one topic
            ParamCheckUtils.validateEventId(topics.length > 1 ? "" : topics[0],
                    offset,
                    fiscoBcosDelegate.getBlockHeight(Long.parseLong(groupId)));
        }
        ParamCheckUtils.validateListenerNotNull(listener);

        for (String topic : topics) {
            if (ParamCheckUtils.isTopicPattern(topic)) {
                ParamCheckUtils.validateTopicPattern(topic);
            } else {
                ParamCheckUtils.validateTopicName(topic);
            }
        }

        log.info("subscribe topics: {}", Arrays.toString(topics));
        return subscribeTopic(topics, Long.valueOf(groupId), offset, interfaceType, listener);
    }

    private static void validateSubscribeTopic(String topic, String groupId, String offset) throws BrokerException {
        if (ParamCheckUtils.isTopicPattern(topic)) {
            ParamCheckUtils.validateTopicPattern(topic);
            if (!offset.equals(WeEvent.OFFSET_FIRST) && !offset.equals(WeEvent.OFFSET_LAST)) {
                // not a topic name
                ParamCheckUtils.validateEventId("", offset, fiscoBcosDelegate.getBlockHeight(Long.parseLong(groupId)));
            }
        } else {
            ParamCheckUtils.validateTopicName(topic);
            if (!offset.equals(WeEvent.OFFSET_FIRST) && !offset.equals(WeEvent.OFFSET_LAST)) {
                ParamCheckUtils.validateEventId(topic, offset, fiscoBcosDelegate.getBlockHeight(Long.parseLong(groupId)));
            }
        }
    }

    @Override
    public String subscribe(String topic, String groupId, String offset, String interfaceType, ConsumerListener listener) throws BrokerException {
        ParamCheckUtils.validateGroupId(groupId);
        ParamCheckUtils.validateOffset(offset);
        ParamCheckUtils.validateListenerNotNull(listener);

        // may be a topic pattern in param topic
        validateSubscribeTopic(topic, groupId, offset);

        log.info("subscribe topic: {} offset: {}", topic, offset);
        return subscribeTopic(topic, Long.valueOf(groupId), offset, interfaceType, listener);
    }

    @Override
    public String subscribe(String topic, String groupId, String offset, String subscriptionId, String interfaceType, ConsumerListener listener) throws BrokerException {
        ParamCheckUtils.validateGroupId(groupId);
        ParamCheckUtils.validateOffset(offset);
        ParamCheckUtils.validateSubscriptionId(subscriptionId);
        ParamCheckUtils.validateListenerNotNull(listener);

        // may be a topic pattern  in param topic
        validateSubscribeTopic(topic, groupId, offset);

        log.info("subscribe topic: {} offset: {} subscriptionId:{}", topic, offset, subscriptionId);
        return subscribeTopic(topic, Long.valueOf(groupId), offset, subscriptionId, interfaceType, listener);
    }

    private String subscribeTopic(String topic, Long groupId, String offset, String interfaceType, ConsumerListener listener) throws BrokerException {
        String[] topics = {topic};
        return subscribeTopic(topics, groupId, offset, interfaceType, listener);
    }

    private String subscribeTopic(String topic, Long groupId, String offset, String subscriptionId, String interfaceType, ConsumerListener listener) throws BrokerException {
        String[] topics = {topic};
        return subscribeTopic(topics, groupId, offset, subscriptionId, interfaceType, listener);
    }

    private String subscribeTopic(String[] topics, Long groupId, String offset, String subscriptionId, String interfaceType, ConsumerListener listener) throws BrokerException {
        // already exist
        if (this.subscriptions.containsKey(subscriptionId)) {
            Subscription subscription = this.subscriptions.get(subscriptionId);
            if (!subscription.getGroupId().equals(groupId)) {
                throw new BrokerException(ErrorCode.TOPIC_NOT_MATCH);
            }
            if (!Arrays.equals(subscription.topics, topics)) {
                throw new BrokerException(ErrorCode.TOPIC_NOT_MATCH);
            }

            if (subscription.getHistoryEventLoop() != null) {
                subscription.getHistoryEventLoop().setOffset(offset);
            }

            return subscription.getUuid();
        }

        if (!fiscoBcosDelegate.listGroupId().contains(groupId)) {
            throw new BrokerException(ErrorCode.WE3SDK_UNKNOWN_GROUP);
        }

        // new subscribe
        Subscription subscription = new Subscription(topics, groupId, offset, interfaceType, listener);
        subscription.resetSubscriptionId(subscriptionId);
        this.subscriptions.put(subscriptionId, subscription);
        this.mainEventLoops.get(groupId).addSubscription(subscription);
        return subscriptionId;
    }

    private String subscribeTopic(String[] topic, Long groupId, String offset, String interfaceType, ConsumerListener listener) throws BrokerException {
        if (!fiscoBcosDelegate.listGroupId().contains(groupId)) {
            throw new BrokerException(ErrorCode.WE3SDK_UNKNOWN_GROUP);
        }

        // new subscribe
        Subscription subscription = new Subscription(topic, groupId, offset, interfaceType, listener);
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
        this.mainEventLoops.get(subscription.getGroupId()).removeSubscription(subscription);
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
        for (Long groupId : fiscoBcosDelegate.listGroupId()) {
            MainEventLoop mainEventLoop = new MainEventLoop(groupId);
            this.threadPoolTaskExecutor.execute(mainEventLoop);
            this.mainEventLoops.put(groupId, mainEventLoop);
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
    public synchronized Map<String, Object> listSubscription() {
        Map<String, Object> subscribeIdList = new HashMap<>();
        for (Map.Entry<String, Subscription> entry : this.subscriptions.entrySet()) {
            Subscription subscription = entry.getValue();
            SubscriptionInfo subscriptionInfo = new SubscriptionInfo();

            subscriptionInfo.setInterfaceType(subscription.getInterfaceType());
            subscriptionInfo.setNotifiedEventCount(subscription.getNotifiedEventCount().toString());
            subscriptionInfo.setNotifyingEventCount(subscription.getNotifyingEventCount().toString());
            subscriptionInfo.setNotifyTimeStamp(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(subscription.getNotifyTimeStamp()));
            subscriptionInfo.setTopicName(Arrays.toString(subscription.getTopics()));
            subscriptionInfo.setSubscribeId(subscription.getUuid());
            subscribeIdList.put(subscription.getUuid(), subscriptionInfo);
        }

        log.debug("subscriptions: {}", this.subscriptions.toString());
        return subscribeIdList;
    }

    /**
     * One topic subscription.
     * ##### more details #####
     * Normally, the events published in the future will be notified by MainEventLoop.
     * If the offset is not WeEvent.OFFSET_LAST, then it need a helper task HistoryEventLoop to fetch history events.
     * It works like as followings:
     * HistoryEventLoop(a few data in the same block of offset -> offset -> target events -> current block) ===> MainEventLoop
     * HistoryEventLoop aim to fetch target event between offset and current block(it will float forward while fetching).
     * It's notify task will switch into MainEventLoop while arriving at highest block height(=current block height in MainEventLoop).
     * But the switch action is done by MainEventLoop in another thread, HistoryEventLoop may float forward a few block at the same time.
     * Because once loop (both in HistoryEventLoop and MainEventLoop) is a long blocking task,
     * so we use a merge cache to avoid repeat notify, not a strict consistency. Like ideas showed in optimistic lock.
     */
    @Data
    class Subscription {

        /**
         * interfaceType restful or json rpc
         */
        private String interfaceType;

        /**
         * Subscription ID
         */
        private String uuid;

        /**
         * Binding topic.
         */
        private String[] topics;

        /**
         * Binding groupId.
         */
        private Long groupId;

        /**
         * event offset.
         */
        private String offset;

        /**
         * Event notify task.
         */
        private NotifyTask notifyTask;

        /**
         * optional, if offset != WeEvent.OFFSET_LAST then need a event loop to fetch history event
         */
        private HistoryEventLoop historyEventLoop;

        /**
         * helper to avoid repeat notify if exist HistoryEventLoop
         */
        private Map<String, WeEvent> mergeCache;

        /**
         * first block in HistoryEventLoop dispatch
         */
        private Long historyBlock;

        @Override
        public String toString() {
            return "Subscription{" +
                    "uuid='" + this.uuid + '\'' +
                    ", topic='" + Arrays.toString(this.topics) + '\'' +
                    ", groupId='" + this.groupId + '\'' +
                    ", offset='" + this.offset + '\'' +
                    '}';
        }

        private Subscription(String[] topics, Long groupId, String offset, String interfaceType, IConsumer.ConsumerListener listener) throws BrokerException {
            this.uuid = UUID.randomUUID().toString();
            this.topics = topics;
            this.groupId = groupId;
            this.offset = offset;
            this.interfaceType = interfaceType;

            this.notifyTask = new NotifyTask(this.uuid, listener);

            // not OFFSET_LAST, need history help task
            if (!this.offset.equals(WeEvent.OFFSET_LAST)) {
                log.info("need history event loop, {}", this);

                this.historyEventLoop = new HistoryEventLoop(this.uuid, topics, groupId, offset, this);
            }
        }

        private Long getNotifyingEventCount() {
            return (long) this.notifyTask.eventQueue.size();
        }

        private Long getNotifiedEventCount() {
            return this.notifyTask.notifiedCount;
        }

        private Date getNotifyTimeStamp() {
            return this.notifyTask.lastTimeStamp;

        }

        private void resetSubscriptionId(String subscriptionId) {
            this.uuid = subscriptionId;
            this.notifyTask.subscriptionId = subscriptionId;
        }

        // mainLoop = true meanings dispatch from MainEventLoop
        private void dispatch(List<WeEvent> events, boolean mainLoop, Long blockHeight) {
            // filter the events
            List<WeEvent> topicEvents = filter(events, this.topics);
            if (topicEvents.isEmpty()) {
                return;
            }

            // always HistoryEventLoop dispatch first, then MainEventLoop
            if (!mainLoop) {
                log.info("dispatch from HistoryEventLoop");

                // have not got offset
                if (this.mergeCache == null) {
                    int offsetIdx = -1;
                    for (int idx = 0; idx < topicEvents.size(); idx++) {
                        if (topicEvents.get(idx).getEventId().equals(this.offset)) {
                            offsetIdx = idx;
                            break;
                        }
                    }

                    // got offset
                    if (offsetIdx >= 0) {
                        log.info("got offset at index: {}", offsetIdx);

                        // get event after offset, exclusive offset itself
                        topicEvents = topicEvents.subList(offsetIdx + 1, topicEvents.size());

                        log.info("HistoryEventLoop initialize merge cache at block: {}", blockHeight);
                        this.historyBlock = blockHeight;
                        this.mergeCache = new HashMap<>();
                    } else {
                        log.info("event list is empty after filter by offset");

                        return;
                    }
                }
            }

            // need merge in cache
            if (this.mergeCache != null) {
                topicEvents.removeIf((event) -> this.mergeCache.containsKey(event.getEventId()));
                for (WeEvent event : topicEvents) {
                    this.mergeCache.put(event.getEventId(), event);
                }

                // cleanup merge cache if needed
                if (mainLoop && blockHeight > this.historyBlock + fiscoConfig.getConsumerHistoryMergeBlock()) {
                    log.info("HistoryEventLoop finalize merge cache at block: {}", fiscoConfig.getConsumerHistoryMergeBlock());

                    this.mergeCache.clear();
                    this.mergeCache = null;
                }
            }

            if (topicEvents.isEmpty()) {
                log.info("event list is empty after filter by merge cache");
                return;
            }

            // notify to remote really
            this.notifyTask.push(topicEvents);
        }

        private synchronized void doStart() {
            threadPoolTaskExecutor.execute(this.notifyTask);
            if (this.historyEventLoop != null) {
                threadPoolTaskExecutor.execute(this.historyEventLoop);
            }
        }

        // can not doStart again after doStop
        private synchronized void doStop() {
            this.notifyTask.doExit();

            // wait task exit really
            idle();

            stopHistory();
        }

        private synchronized void stopHistory() {
            if (this.historyEventLoop != null) {
                this.historyEventLoop.doExit();
                this.historyEventLoop = null;
            }
        }

        private synchronized boolean tryStopHistory(Long mainLastBlock) {
            if (mainLastBlock > 0 && mainLastBlock <= this.historyEventLoop.lastBlock) {
                log.info("switch history to main event loop, {} ---> {}, {}",
                        this.historyEventLoop.lastBlock, mainLastBlock, this);

                stopHistory();
                return true;
            }

            return false;
        }
    }

    /**
     * Notify task run in unique thread.
     */
    class NotifyTask extends StoppableTask {
        private String subscriptionId;
        private IConsumer.ConsumerListener consumerListener;

        private BlockingDeque<WeEvent> eventQueue = new LinkedBlockingDeque<>();
        private Long notifiedCount = 0L;
        private Date lastTimeStamp = new Date();

        private NotifyTask(String subscriptionId, IConsumer.ConsumerListener consumerListener) {
            super("event-notify@" + subscriptionId);

            this.subscriptionId = subscriptionId;
            this.consumerListener = consumerListener;
        }

        private void push(List<WeEvent> events) {
            try {
                // offer event into queue one by one
                for (WeEvent event : events) {
                    if (!this.eventQueue.offer(event, idleTime, TimeUnit.MILLISECONDS)) {
                        log.error("push notify failed due to queue is full");
                        this.consumerListener.onException(new BrokerException(ErrorCode.SUBSCRIPTION_NOTIFY_QUEUE_FULL));
                        return;
                    }
                    log.debug("offer notify queue, event: {}", event);
                }
            } catch (InterruptedException e) {
                log.error("offer notify queue failed", e);
            }
        }

        @Override
        protected void taskOnceLoop() {
            try {
                WeEvent event = this.eventQueue.poll(idleTime, TimeUnit.MILLISECONDS);
                // Empty queue, try next.
                if (event == null) {
                    return;
                }
                log.debug("poll from notify queue, event: {}", event);

                // call back once for a single event
                this.consumerListener.onEvent(this.subscriptionId, event);
                this.notifiedCount++;
                this.lastTimeStamp = new Date();
            } catch (Exception e) {
                this.consumerListener.onException(e);
            }
        }
    }

    /**
     * History event loop task within unique thread.
     */
    class HistoryEventLoop extends StoppableTask {
        /**
         * Last detected block.
         */
        private Long lastBlock = 0L;

        /**
         * Cached value for highest block height.
         */
        private Long cachedBlockHeight = 0L;

        private String[] topics;

        private Long groupId;

        /**
         * binding to dispatch event
         */
        private Subscription subscription;

        private void setOffset(String offset) throws BrokerException {
            switch (offset) {
                case WeEvent.OFFSET_FIRST:
                    this.lastBlock = 0L;
                    break;

                case WeEvent.OFFSET_LAST:
                    log.warn("assert !WeEvent.OFFSET_LAST in HistoryEventLoop");
                    throw new BrokerException(ErrorCode.UNKNOWN_ERROR);

                default:
                    this.lastBlock = DataTypeUtils.decodeBlockNumber(offset) - 1;
                    break;
            }
        }

        private HistoryEventLoop(String subscriptionId, String[] topics, Long groupId, String offset, Subscription subscription) throws BrokerException {
            super("history-event-loop@" + subscriptionId);

            this.topics = topics;
            this.groupId = groupId;
            this.subscription = subscription;

            this.setOffset(offset);
            log.info("HistoryEventLoop initialized with last block: {} in group: {}", this.lastBlock, this.groupId);
        }

        @Override
        protected void taskOnceLoop() {
            try {
                // the block try to deal with in this one loop
                Long currentBlock = this.lastBlock + 1;

                // cache may be expired, refresh it
                if (currentBlock > this.cachedBlockHeight) {
                    Long blockHeight = fiscoBcosDelegate.getBlockHeight(groupId);
                    if (blockHeight <= 0) {
                        // Don't try too fast if net error.
                        idle();
                        return;
                    }
                    this.cachedBlockHeight = blockHeight;

                    // no new block
                    if (currentBlock > this.cachedBlockHeight) {
                        log.debug("no new block in group: {}, idle", this.groupId);
                        idle();
                        return;
                    }
                }

                // loop one block, and filter target event with topic
                log.debug("history event loop in group: {}, topics: {} cached block height: {}", this.groupId, Arrays.toString(topics), this.cachedBlockHeight);
                List<WeEvent> events = filterBlockEvent(currentBlock, topics, groupId);
                log.debug("history event loop done, block: {} event size: {}", currentBlock, events.size());

                // it there is event to notify
                if (!events.isEmpty()) {
                    this.subscription.dispatch(events, false, currentBlock);
                }

                // next block
                this.lastBlock = currentBlock;
            } catch (BrokerException e) {
                log.error("history event loop exception in group: " + this.groupId, e);
                this.subscription.getNotifyTask().consumerListener.onException(e);
            }
        }
    }

    /**
     * Detect new event from target group
     */
    class MainEventLoop extends StoppableTask {
        // binding group
        private Long groupId;

        // last well done block
        private Long lastBlock;

        // subscription in main loop
        private List<String> mainSubscriptionIds = new ArrayList<>();

        // subscription in history loop
        private List<String> historySubscriptionIds = new ArrayList<>();

        private MainEventLoop(Long groupId) throws BrokerException {
            super("main-event-loop-" + groupId);
            this.groupId = groupId;

            // get last block
            Long blockHeight = fiscoBcosDelegate.getBlockHeight(this.groupId);
            if (blockHeight <= 0) {
                throw new BrokerException(ErrorCode.WE3SDK_INIT_ERROR);
            }

            // init last block
            this.lastBlock = blockHeight;
            log.info("MainEventLoop initialized with last block: {} in group: {}", this.lastBlock, this.groupId);
        }

        private synchronized void doStop() {
            log.info("try to stop MainEventLoop in group: {}", this.groupId);

            // stop main loop first
            this.doExit();

            for (String subscriptionId : this.mainSubscriptionIds) {
                if (subscriptions.containsKey(subscriptionId)) {
                    subscriptions.get(subscriptionId).doStop();
                }
            }

            for (String subscriptionId : this.historySubscriptionIds) {
                if (subscriptions.containsKey(subscriptionId)) {
                    subscriptions.get(subscriptionId).doStop();
                }
            }
        }

        private synchronized void addSubscription(Subscription subscription) {
            subscription.doStart();

            if (subscription.getHistoryEventLoop() == null) {
                this.mainSubscriptionIds.add(subscription.getUuid());
            } else {
                this.historySubscriptionIds.add(subscription.getUuid());
            }
        }

        private synchronized void removeSubscription(Subscription subscription) {
            if (subscription.getHistoryEventLoop() == null) {
                this.mainSubscriptionIds.remove(subscription.getUuid());
            } else {
                this.historySubscriptionIds.remove(subscription.getUuid());
            }

            subscription.doStop();
        }

        private synchronized void mergeHistory() {
            List<String> stopped = new ArrayList<>();
            for (String subscriptionId : this.historySubscriptionIds) {
                if (subscriptions.containsKey(subscriptionId)) {
                    // try to stop history if needed
                    if (subscriptions.get(subscriptionId).tryStopHistory(this.lastBlock)) {
                        stopped.add(subscriptionId);
                    }
                }
            }

            // switch stopped history to main loop
            if (!stopped.isEmpty()) {
                this.historySubscriptionIds.removeAll(stopped);
                this.mainSubscriptionIds.addAll(stopped);
            }
        }

        private synchronized void dispatch(List<WeEvent> events, Long blockHeight) {
            for (String subscriptionId : this.mainSubscriptionIds) {
                subscriptions.get(subscriptionId).dispatch(events, true, blockHeight);
            }
        }

        private synchronized void dispatch(Throwable e) {
            for (String subscriptionId : this.mainSubscriptionIds) {
                subscriptions.get(subscriptionId).getNotifyTask().consumerListener.onException(e);
            }
        }

        @Override
        protected void taskOnceLoop() {
            try {
                // the block try to deal with in this one loop
                Long currentBlock = this.lastBlock + 1;

                // get block height
                Long blockHeight = fiscoBcosDelegate.getBlockHeight(this.groupId);
                if (blockHeight <= 0) {
                    // Don't try too fast if net error.
                    idle();
                    return;
                }
                // no new block
                if (currentBlock > blockHeight) {
                    log.debug("no new block in group: {}, idle", this.groupId);
                    idle();
                    return;
                }

                // merge history if needed
                this.mergeHistory();

                // no need to fetch event if no subscription
                if (this.mainSubscriptionIds.isEmpty()) {
                    idle();
                } else {
                    // fetch all event from block chain in this block
                    log.debug("fetch events from block height: {} in group: {}", currentBlock, this.groupId);
                    List<WeEvent> events = fiscoBcosDelegate.loop(currentBlock, this.groupId);
                    // idle until get event information(include empty)
                    if (events == null) {
                        idle();
                        return;
                    }
                    log.debug("fetch done, block: {} event size: {}", currentBlock, events.size());

                    this.dispatch(events, currentBlock);
                }

                // next block
                this.lastBlock = currentBlock;
            } catch (BrokerException e) {
                log.error("main event loop exception in group: " + this.groupId, e);
                this.dispatch(e);
            }
        }
    }
}
