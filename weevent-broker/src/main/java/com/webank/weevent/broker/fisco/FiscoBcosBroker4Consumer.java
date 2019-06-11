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
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

/**
 * Event broker's consumer implement in fisco-bcos.
 *
 * @author matthewliu
 * @since 2018/11/02
 */
@Slf4j
public class FiscoBcosBroker4Consumer extends FiscoBcosTopicAdmin implements IConsumer {

    /**
     * Subscription ID <-> Subscription
     */
    private Map<String, Subscription> subscriptions = new HashMap<>();

    /**
     * Group ID <-> MainEventLoop
     */
    private Map<Long, MainEventLoop> mainEventLoops = new HashMap<>();

    /**
     * Whether the Consumer has started
     */
    private boolean consumerStarted = false;

    /**
     * @see com.webank.weevent.broker.config.WeEventConfig#consumerIdleTime
     */
    private int idleTime;

    public FiscoBcosBroker4Consumer() {
        super();

        this.idleTime = BrokerApplication.weEventConfig.getConsumerIdleTime();
    }

    /**
     * Idle caller thread
     *
     * @throws InterruptedException InterruptedException
     */
    private void idle() throws InterruptedException {
        // Transaction commit every 1 second.
        Thread.sleep(this.idleTime);
    }

    /**
     * getEventSeq
     *
     * @param eventId the eventId
     * @return Long return 0L if error
     */
    private Long getEventSeq(String eventId) {
        if (eventId != null) {
            try {
                return DataTypeUtils.decodeSeq(eventId);
            } catch (BrokerException e) {
                log.error("invalid eventId: {}", eventId);
            }
        }

        return 0L;
    }

    /**
     * Idle if net error in loop.
     *
     * @param blockNum the blockNum
     * @return WeEvent list
     */
    private List<WeEvent> loopBlock(Long blockNum, String[] topics, Long groupId)
            throws BrokerException, InterruptedException {
        // idle until get event
        List<WeEvent> blockEventsList = null;
        while (blockEventsList == null) {
            blockEventsList = fiscoBcosDelegate.loop(blockNum, groupId);
            if (blockEventsList == null) {
                idle();
            }
        }

        // get target event
        List<WeEvent> topicEventsList = new ArrayList<>();
        for (WeEvent event : blockEventsList) {
            for (String topic : topics) {
                if (ParamCheckUtils.isTopicPattern(topic)) {
                    if (WeEventUtils.match(event.getTopic(), topic)) {
                        topicEventsList.add(event);
                    }
                } else {
                    if (topic.equals(event.getTopic())) {
                        topicEventsList.add(event);
                    }
                }
            }
        }

        return topicEventsList;
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
            ParamCheckUtils.validateEventId("", offset, fiscoBcosDelegate.getBlockHeight(Long.parseLong(groupId)));
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

    @Override
    public String subscribe(String topic, String groupId, String offset, String interfaceType, ConsumerListener listener) throws BrokerException {
        if (ParamCheckUtils.isTopicPattern(topic)) {
            ParamCheckUtils.validateTopicPattern(topic);
        } else {
            ParamCheckUtils.validateTopicName(topic);
        }

        ParamCheckUtils.validateGroupId(groupId);
        ParamCheckUtils.validateOffset(offset);
        if (!offset.equals(WeEvent.OFFSET_FIRST) && !offset.equals(WeEvent.OFFSET_LAST)) {
            ParamCheckUtils.validateEventId(topic, offset, fiscoBcosDelegate.getBlockHeight(Long.parseLong(groupId)));
        }
        ParamCheckUtils.validateListenerNotNull(listener);

        log.info("subscribe topic: {} offset: {}", topic, offset);
        return subscribeTopic(topic, Long.valueOf(groupId), offset, interfaceType, listener);
    }

    @Override
    public String subscribe(String topic, String groupId, String offset, String subscriptionId, String interfaceType, ConsumerListener listener) throws BrokerException {
        if (ParamCheckUtils.isTopicPattern(topic)) {
            ParamCheckUtils.validateTopicPattern(topic);
        } else {
            ParamCheckUtils.validateTopicName(topic);
        }
        ParamCheckUtils.validateOffset(offset);
        ParamCheckUtils.validateListenerNotNull(listener);
        ParamCheckUtils.validateSubscriptionId(subscriptionId);
        ParamCheckUtils.validateGroupId(groupId);
        if (!offset.equals(WeEvent.OFFSET_FIRST) && !offset.equals(WeEvent.OFFSET_LAST)) {
            ParamCheckUtils.validateEventId(topic, offset, fiscoBcosDelegate.getBlockHeight(Long.parseLong(groupId)));
        }

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
        try {
            UUID.fromString(subscriptionId);
        } catch (IllegalArgumentException e) {
            throw new BrokerException(ErrorCode.SUBSCRIPTIONID_FORMAT_INVALID);
        }

        // already exist
        if (this.subscriptions.containsKey(subscriptionId)) {
            Subscription subscription = this.subscriptions.get(subscriptionId);
            if (!Arrays.equals(subscription.topics, topics)) {
                throw new BrokerException(ErrorCode.TOPIC_NOT_MATCH);
            }

            if (subscription.getHistoryEventLoop() != null) {
                subscription.getHistoryEventLoop().setOffset(offset);
                subscription.getHistoryEventLoop().setGroupId(groupId);
            }

            return subscription.uuid;
        }

        // new subscribe
        Subscription subscription = new Subscription(topics, groupId, offset, interfaceType, listener);
        subscription.resetSubscriptionId(subscriptionId);
        this.subscriptions.put(subscriptionId, subscription);
        this.mainEventLoops.get(groupId).addSubscription(subscription);
        return subscriptionId;
    }

    private String subscribeTopic(String[] topic, Long groupId, String offset, String interfaceType, ConsumerListener listener) throws BrokerException {
        Subscription subscription = new Subscription(topic, groupId, offset, interfaceType, listener);
        this.subscriptions.put(subscription.uuid, subscription);
        this.mainEventLoops.get(groupId).addSubscription(subscription);
        return subscription.uuid;
    }

    @Override
    public boolean unSubscribe(String subscriptionId) throws BrokerException {
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
    public boolean startConsumer() throws BrokerException {
        if (this.consumerStarted) {
            throw new BrokerException(ErrorCode.CONSUMER_ALREADY_STARTED);
        }

        // load MainEventLoop with configuration
        for (Long groupId : fiscoBcosDelegate.listGroupId()) {
            MainEventLoop mainEventLoop = new MainEventLoop(groupId);
            mainEventLoop.start();
            this.mainEventLoops.put(groupId, mainEventLoop);
        }

        this.consumerStarted = true;
        log.info("start consumer finish");
        return true;
    }

    @Override
    public boolean shutdownConsumer() {
        // stop main event loop and referred subscription
        for (Map.Entry<Long, MainEventLoop> mainEventLoop : this.mainEventLoops.entrySet()) {
            mainEventLoop.getValue().doStop();
        }
        this.mainEventLoops.clear();
        this.subscriptions.clear();

        this.consumerStarted = false;

        log.info("shut down consumer finish");
        return true;
    }

    @Override
    public Map<String, Object> listSubscription() {
        Map<String, Object> subscribeIdList = new HashMap<>();
        for (Map.Entry<String, Subscription> entry : this.subscriptions.entrySet()) {
            SubscriptionInfo subscriptionInfo = new SubscriptionInfo();
            subscriptionInfo.setInterfaceType(entry.getValue().getInterfaceType());
            subscriptionInfo.setNotifiedEventCount(entry.getValue().getNotifiedEventCount().toString());
            subscriptionInfo.setNotifyingEventCount(entry.getValue().getNotifyingEventCount().toString());
            subscriptionInfo.setNotifyTimeStamp(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(entry.getValue().getNotifyTimeStamp()));
            subscriptionInfo.setTopicName(Arrays.toString(entry.getValue().getTopics()));
            subscriptionInfo.setSubscribeId(entry.getValue().getUuid());
            subscribeIdList.put(entry.getValue().getUuid(), subscriptionInfo);
        }

        log.debug("subscriptions: {}", this.subscriptions.toString());
        return subscribeIdList;
    }

    /**
     * One topic subscription.
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
         * Event offset.
         */
        private String offset;

        /**
         * Event notify task.
         */
        private NotifyTask notifyTask;

        /**
         * optional, if offset != WeEvent.OFFSET_LAST then it's called history
         */
        private HistoryEventLoop historyEventLoop;

        @Override
        public String toString() {
            return "Subscription{" +
                    "uuid='" + this.uuid + '\'' +
                    ", topic='" + Arrays.toString(this.topics) + '\'' +
                    ", groupId='" + this.groupId + '\'' +
                    '}';
        }

        Subscription(String[] topics, Long groupId, String offset, String interfaceType, IConsumer.ConsumerListener listener) throws BrokerException {
            this.uuid = UUID.randomUUID().toString();
            this.topics = topics;
            this.groupId = groupId;
            this.offset = offset;
            this.interfaceType = interfaceType;

            this.notifyTask = new NotifyTask(this.uuid, listener);
            if (!this.offset.equals(WeEvent.OFFSET_LAST)) {
                this.historyEventLoop = new HistoryEventLoop(topics, groupId, offset, this.notifyTask);
                this.historyEventLoop.setOffset(offset);
            }
        }

        Long getNotifyingEventCount() {
            return (long) this.notifyTask.getEventQueue().size();
        }

        Long getNotifiedEventCount() {
            return this.notifyTask.getNotifiedCount();
        }

        Date getNotifyTimeStamp() {
            return this.notifyTask.getLastTimeStamp();

        }

        void resetSubscriptionId(String subscriptionId) {
            this.uuid = subscriptionId;
            this.notifyTask.setSubscriptionId(subscriptionId);
        }

        void dispatch(List<WeEvent> events) {
            // filter the events
            List<WeEvent> topicEvents = new ArrayList<>();
            for (WeEvent event : events) {
                for (String topic : this.topics) {
                    if (ParamCheckUtils.isTopicPattern(topic)) {
                        if (WeEventUtils.match(event.getTopic(), topic)) {
                            topicEvents.add(event);
                        }
                    } else {
                        if (topic.equals(event.getTopic())) {
                            topicEvents.add(event);
                        }
                    }
                }
            }

            this.notifyTask.push(topicEvents);
        }

        void doStart() {
            this.notifyTask.start();
            if (this.historyEventLoop != null) {
                this.historyEventLoop.start();
            }
        }

        // can not doStart again after doStop
        void doStop() {
            try {
                if (this.historyEventLoop != null) {
                    this.historyEventLoop.doExit();
                    this.historyEventLoop.join();
                    this.historyEventLoop = null;
                }

                if (this.notifyTask != null) {
                    this.notifyTask.doExit();
                    this.notifyTask.join();
                    this.notifyTask = null;
                }
            } catch (InterruptedException e) {
                log.error("stop task failed", e);
            }
        }
    }

    /**
     * Notify task within unique thread.
     */
    @Data
    @EqualsAndHashCode(callSuper = true)
    class NotifyTask extends StoppableTask {
        private String subscriptionId;
        private Long lastEventSeq = 0L;
        private IConsumer.ConsumerListener consumerListener;

        private BlockingDeque<WeEvent> eventQueue = new LinkedBlockingDeque<>();
        private Long notifiedCount = 0L;
        private Date lastTimeStamp = new Date();

        NotifyTask(String subscriptionId, IConsumer.ConsumerListener consumerListener) {
            super("event-notify");

            this.subscriptionId = subscriptionId;
            this.consumerListener = consumerListener;
        }

        void push(List<WeEvent> events) {
            // Skip event if needed.
            if (this.lastEventSeq > 0) {
                events.removeIf(node -> (getEventSeq(node.getEventId()) <= this.lastEventSeq));
            }

            try {
                for (WeEvent event : events) {
                    if (!this.eventQueue.offer(event, idleTime, TimeUnit.MILLISECONDS)) {
                        log.error("push notify failed due to queue is full");
                        this.consumerListener.onException(new BrokerException(ErrorCode.SUBSCRIPTION_NOTIFY_QUEUE_FULL));
                        return;
                    }
                    log.debug("offer notify queue, event: {}", event);
                    // Ordered by event seq.
                    this.lastEventSeq = getEventSeq(event.getEventId());
                }
            } catch (InterruptedException e) {
                log.error("offer notify queue failed", e);
            }
        }

        @Override
        protected void taskOnceLoop() throws InterruptedException {
            if (this.consumerListener != null) {
                try {
                    WeEvent event = this.eventQueue.poll(idleTime, TimeUnit.MILLISECONDS);
                    // Empty queue, try next.
                    if (event == null) {
                        return;
                    }
                    log.debug("poll from notify queue, event: {}", event);

                    this.consumerListener.onEvent(this.subscriptionId, event);

                    this.notifiedCount++;
                    this.lastTimeStamp = new Date();
                } catch (InterruptedException e) {
                    throw e;
                } catch (Exception e) {
                    this.consumerListener.onException(e);
                }
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
        private Long lastBlock = 0L;

        // subscriptionId
        private List<String> subscriptionIds = new ArrayList<>();

        MainEventLoop(Long groupId) {
            super("main-event-loop-" + groupId);
            this.groupId = groupId;
        }

        void doStop() {
            for (String subscriptionId : this.subscriptionIds) {
                if (subscriptions.containsKey(subscriptionId)) {
                    subscriptions.get(subscriptionId).doStop();
                }
            }

            this.doExit();
        }

        void addSubscription(Subscription subscription) {
            subscription.doStart();
            this.subscriptionIds.add(subscription.getUuid());
        }

        void removeSubscription(Subscription subscription) {
            subscription.doStop();
            this.subscriptionIds.remove(subscription.getUuid());
        }

        void mergeHistory() {
            for (String subscriptionId : this.subscriptionIds) {
                if (subscriptions.containsKey(subscriptionId)) {
                    Subscription subscription = subscriptions.get(subscriptionId);
                    if (subscription.getHistoryEventLoop() != null) {
                        if (subscription.getHistoryEventLoop().lastBlock >= this.lastBlock) {
                            log.info("switch history to main event loop, subscription: {}", subscriptionId);

                            // switch history to main loop
                            subscription.getHistoryEventLoop().doExit();
                            subscription.setHistoryEventLoop(null);
                        }
                    }
                }
            }
        }

        void dispatch(List<WeEvent> events) {
            for (String subscriptionId : this.subscriptionIds) {
                if (subscriptions.get(subscriptionId).getHistoryEventLoop() == null) {
                    subscriptions.get(subscriptionId).dispatch(events);
                }
            }
        }

        void dispatch(Throwable e) {
            for (String subscriptionId : this.subscriptionIds) {
                if (subscriptions.get(subscriptionId).getHistoryEventLoop() == null) {
                    subscriptions.get(subscriptionId).getNotifyTask().getConsumerListener().onException(e);
                }
            }
        }

        @Override
        protected void taskOnceLoop() throws InterruptedException {
            try {
                // get current block
                Long currentBlock = fiscoBcosDelegate.getBlockHeight(this.groupId);
                if (currentBlock <= 0) {
                    // Don't try too fast if net error.
                    idle();
                    return;
                }

                // no new block
                if (currentBlock <= this.lastBlock) {
                    idle();
                    return;
                }

                // merge history if needed
                this.mergeHistory();

                // skip if no subscription
                if (this.subscriptionIds.isEmpty()) {
                    return;
                }

                // Fetch all event from this block.
                log.debug("fetch events from block height: {}", currentBlock);
                List<WeEvent> events = null;
                while (events == null) {
                    events = fiscoBcosDelegate.loop(currentBlock, this.groupId);
                    // idle until get event information(include empty)
                    if (events == null) {
                        idle();
                    }
                }
                log.debug("fetch done, block: {} event size: {}", currentBlock, events.size());

                this.dispatch(events);

                // next block.
                this.lastBlock = currentBlock;
            } catch (BrokerException e) {
                log.error("main event loop exception", e);
                this.dispatch(e);
            }
        }
    }

    /**
     * Event loop task within unique thread.
     */
    class HistoryEventLoop extends StoppableTask {
        /**
         * Lasted detected block.
         */
        private Long lastBlock = 0L;

        /**
         * Cached value for highest block height.
         */
        private Long cachedBlockHeight = 0L;

        private String[] topics;

        private Long groupId;

        private NotifyTask notifyTask;

        public void setGroupId(Long groupId) {
            this.groupId = groupId;
        }

        public void setOffset(String offset) throws BrokerException {
            Long lastEventSeq = 0L;

            switch (offset) {
                case WeEvent.OFFSET_FIRST:
                    this.lastBlock = 0L;
                    break;

                case WeEvent.OFFSET_LAST:
                    this.lastBlock = -1L;
                    break;

                default:
                    this.lastBlock = DataTypeUtils.decodeBlockNumber(offset) - 1;
                    lastEventSeq = DataTypeUtils.decodeSeq(offset);
            }

            this.notifyTask.setLastEventSeq(lastEventSeq);
        }

        HistoryEventLoop(String[] topics, Long groupId, String offset, NotifyTask notifyTask) throws BrokerException {
            super("history-event-loop");

            this.topics = topics;
            this.groupId = groupId;
            this.notifyTask = notifyTask;

            this.setOffset(offset);
        }

        @Override
        protected void taskOnceLoop() throws InterruptedException {
            try {
                Long currentBlock = this.lastBlock + 1;
                // Cache may be expired, refresh it.
                if (currentBlock > this.cachedBlockHeight) {
                    Long blockHeight = fiscoBcosDelegate.getBlockHeight(groupId);
                    if (blockHeight <= 0) {
                        // Don't try too fast if net error.
                        idle();
                        return;
                    }
                    this.cachedBlockHeight = blockHeight;

                    // No new block.
                    if (currentBlock > this.cachedBlockHeight) {
                        log.debug("no new block, idle");
                        idle();
                        return;
                    }
                }

                // Normal loop one block as following.
                log.debug("history loop, topics: {} cached block height: {}", Arrays.toString(topics), this.cachedBlockHeight);
                List<WeEvent> events = loopBlock(currentBlock, topics, groupId);
                log.debug("history loop done, block: {} event size: {}", currentBlock, events.size());

                this.notifyTask.push(events);

                // Init for next block.
                this.lastBlock = currentBlock;
            } catch (BrokerException e) {
                this.notifyTask.getConsumerListener().onException(e);
            }
        }
    }
}

