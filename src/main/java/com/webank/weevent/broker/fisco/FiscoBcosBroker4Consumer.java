package com.webank.weevent.broker.fisco;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import com.webank.weevent.BrokerApplication;
import com.webank.weevent.broker.plugin.IConsumer;
import com.webank.weevent.broker.fisco.constant.WeEventConstants;
import com.webank.weevent.broker.fisco.dto.SubscriptionInfo;
import com.webank.weevent.broker.fisco.util.DataTypeUtils;
import com.webank.weevent.broker.fisco.util.ParamCheckUtils;
import com.webank.weevent.broker.fisco.util.SerializeUtils;
import com.webank.weevent.broker.fisco.util.StoppableTask;
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
     * Whether the Consumer has started
     */
    private boolean consumerStarted = false;

    /**
     * @see com.webank.weevent.broker.config.WeEventConfig#consumerIdleTime
     */
    private int idleTime;

    public FiscoBcosBroker4Consumer() {
        this.idleTime = BrokerApplication.weEventConfig.getConsumerIdleTime();
        log.info("read from configuration, idleTime: {} ", this.idleTime);
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
        Long lastSeq = 0L;
        if (eventId == null) {
            return lastSeq;
        }
        try {
            lastSeq = DataTypeUtils.decodeSeq(eventId);
        } catch (BrokerException e) {
            log.error("eventId is illegal:{}", e.getMessage());
        }
        return lastSeq;
    }

    /**
     * Idle if net error in loop.
     *
     * @param blockNum the blockNum
     * @return java.util.List<com.webank.weevent.sdk.WeEvent>
     */
    private List<WeEvent> loopBlock(Long blockNum, String topic)
            throws BrokerException, InterruptedException {
        List<WeEvent> blockEventsList = null;
        List<WeEvent> topicEventsList = new ArrayList<>();
        while (blockEventsList == null) {
            blockEventsList = this.topicService.loop(blockNum);
            if (blockEventsList == null) {
                idle();
            } else {
                for (WeEvent event : blockEventsList) {
                    if (topic.equals(event.getTopic())) {
                        topicEventsList.add(event);
                    }
                }
            }
        }

        return topicEventsList;
    }

    private void validateTopicModelMap(Map<String, String> topics) throws BrokerException {
        if (topics == null || topics.isEmpty()) {
            throw new BrokerException(ErrorCode.TOPIC_MODEL_MAP_IS_NULL);
        }

        Long blockHeight = topicService.getBlockHeight();
        for (Map.Entry<String, String> entry : topics.entrySet()) {
            ParamCheckUtils.validateTopicName(entry.getKey());
            // check topic exist
            if (!exist(entry.getKey())) {
                throw new BrokerException(ErrorCode.TOPIC_NOT_EXIST);
            }

            // check offset
            if (entry.getValue() == null) {
                throw new BrokerException(ErrorCode.TOPIC_MODEL_MAP_IS_NULL);
            }
            if ((!entry.getValue().equals(WeEvent.OFFSET_FIRST) && !entry.getValue().equals(WeEvent.OFFSET_LAST))) {
                ParamCheckUtils.validateEventId(entry.getKey(), entry.getValue(), blockHeight);
            }
        }
    }

    @Override
    public Map<String, String> subscribe(Map<String, String> topics, String interfaceType, ConsumerListener listener) throws BrokerException {
        ParamCheckUtils.validateListenerNotNull(listener);

        // check params in map
        validateTopicModelMap(topics);

        Map<String, String> subscriptions = new HashMap<>();
        for (Map.Entry<String, String> entry : topics.entrySet()) {
            String subscriptionId = subscribeTopic(entry.getKey(), entry.getValue(), interfaceType, listener);
            subscriptions.put(entry.getKey(), subscriptionId);
        }

        log.info("subscribe topics: {}", topics);
        return subscriptions;
    }

    private String subscribeTopic(String topic, String offset, String subscriptionId, String interfaceType, ConsumerListener listener) throws BrokerException {
        try {
            UUID.fromString(subscriptionId);
        } catch (IllegalArgumentException e) {
            throw new BrokerException(ErrorCode.SUBSCRIPTIONID_FORMAT_INVALID);
        }

        if (!this.subscriptions.containsKey(subscriptionId)) {
            Subscription subscription = new Subscription(topic, offset, interfaceType, listener);
            subscription.setUuid(subscriptionId);
            subscription.notifyLoop.setSubscriptionId(subscriptionId);
            subscription.notifyLoop.start();
            subscription.eventDetectLoop.start();
            this.subscriptions.put(subscriptionId, subscription);
            return subscriptionId;
        }

        if (!this.subscriptions.get(subscriptionId).topic.equals(topic)) {
            throw new BrokerException(ErrorCode.TOPIC_NOT_MATCH);
        }
        this.subscriptions.get(subscriptionId).eventDetectLoop.setOffset(offset);
        return this.subscriptions.get(subscriptionId).uuid;
    }

    private String subscribeTopic(String topic, String offset, String interfaceType, ConsumerListener listener) throws BrokerException {
        Subscription subscription = new Subscription(topic, offset, interfaceType, listener);
        subscription.notifyLoop.start();
        subscription.eventDetectLoop.start();

        this.subscriptions.put(subscription.uuid, subscription);
        return subscription.uuid;
    }

    @Override
    public String subscribe(String topic, String offset, String interfaceType, ConsumerListener listener) throws BrokerException {
        ParamCheckUtils.validateOffset(offset);
        ParamCheckUtils.validateTopicName(topic);
        ParamCheckUtils.validateListenerNotNull(listener);
        if (!exist(topic)) {
            throw new BrokerException(ErrorCode.TOPIC_NOT_EXIST);
        }
        if (!offset.equals(WeEvent.OFFSET_FIRST) && !offset.equals(WeEvent.OFFSET_LAST)) {
            ParamCheckUtils.validateEventId(topic, offset, topicService.getBlockHeight());
        }

        log.info("subscribe topics: {} offset: {}", topic, offset);
        return subscribeTopic(topic, offset, interfaceType, listener);
    }

    @Override
    public String subscribe(String topic, String offset, String subscriptionId, String interfaceType, ConsumerListener listener) throws BrokerException {
        ParamCheckUtils.validateOffset(offset);
        ParamCheckUtils.validateTopicName(topic);
        ParamCheckUtils.validateListenerNotNull(listener);
        ParamCheckUtils.validateSubscriptionId(subscriptionId);

        if (!exist(topic)) {
            throw new BrokerException(ErrorCode.TOPIC_NOT_EXIST);
        }
        if (!offset.equals(WeEvent.OFFSET_FIRST) && !offset.equals(WeEvent.OFFSET_LAST)) {
            ParamCheckUtils.validateEventId(topic, offset, topicService.getBlockHeight());
        }

        log.info("subscribe topics: {} offset: {} subscriptionId:{}", topic, offset, subscriptionId);
        return subscribeTopic(topic, offset, subscriptionId, interfaceType, listener);
    }

    @Override
    public boolean unSubscribe(String subscriptionId) throws BrokerException {
        if (!this.subscriptions.containsKey(subscriptionId)) {
            log.warn("not exist subscriptionId {}", subscriptionId);
            throw new BrokerException(ErrorCode.SUBSCRIPTIONID_NOT_EXIST);
        }

        try {
            Subscription subscription = this.subscriptions.get(subscriptionId);
            subscription.stop();
        } catch (InterruptedException e) {
            log.error("unSubscribe topic failed due to InterruptedException", e);
            Thread.currentThread().interrupt();
            return false;
        }

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

        this.consumerStarted = true;
        return true;
    }

    @Override
    public boolean shutdownConsumer() {
        try {
            for (Map.Entry<String, Subscription> entry : this.subscriptions.entrySet()) {
                entry.getValue().stop();
            }
        } catch (InterruptedException e) {
            log.error("shutdown consumer failed due to InterruptedException", e);
            Thread.currentThread().interrupt();
            return false;
        }

        this.subscriptions.clear();
        this.consumerStarted = false;
        return true;
    }

    @Override
    public Map<String, Object> getInnerSubscription() {
        Map<String, Object> subscribeIdList = new HashMap<>();
        for (Map.Entry<String, Subscription> entry : this.subscriptions.entrySet()) {
            SubscriptionInfo subscriptionInfo = new SubscriptionInfo();
            subscriptionInfo.setInterfaceType(entry.getValue().getInterfaceType());
            subscriptionInfo.setNotifiedEventCount(entry.getValue().getNotifiedEventCount().toString());
            subscriptionInfo.setNotifyingEventCount(entry.getValue().getNotifyingEventCount().toString());
            subscriptionInfo.setNotifyTimeStamp(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(entry.getValue().getNotifyTimeStamp()));
            subscriptionInfo.setTopicName(entry.getValue().getTopic());
            subscriptionInfo.setSubscribeId(entry.getValue().getUuid());
            subscribeIdList.put(entry.getValue().getUuid(), subscriptionInfo);
        }
        log.debug("subscriptionMapData:{}", this.subscriptions.toString());
        return subscribeIdList;
    }

    /**
     * One topic subscription.
     */
    @Data
    class Subscription {
        Subscription(String topic, String offset, String interfaceType, IConsumer.ConsumerListener listener) {
            this.uuid = UUID.randomUUID().toString();
            this.topic = topic;
            this.offset = offset;
            this.notifyLoop = new NotifyLoop(this.uuid, listener);
            this.eventDetectLoop = new EventDetectLoop(topic, offset, this.notifyLoop);
            this.interfaceType = interfaceType;
        }

        public Long getNotifyingEventCount() {
            return (long) this.notifyLoop.getEventQueue().size();
        }

        public Long getNotifiedEventCount() {
            return this.notifyLoop.getNotifyingEventCount();
        }

        public Date getNotifyTimeStamp() {
            return this.notifyLoop.getNotifyTimeStamp();

        }

        public synchronized void stop() throws InterruptedException {
            if (this.eventDetectLoop != null) {
                this.eventDetectLoop.doExit();
                this.eventDetectLoop.join();
                this.eventDetectLoop = null;
            }

            if (this.notifyLoop != null) {
                this.notifyLoop.doExit();
                this.notifyLoop.join();
                this.notifyLoop = null;
            }
        }

        /**
         * interfaceType restful or jsonrpc
         */
        private String interfaceType;

        /**
         * Subscription ID
         */
        private String uuid;

        /**
         * Binding topic.
         */
        private String topic;

        /**
         * Event offset.
         */
        private String offset;

        /**
         * Event loop task.
         */
        private EventDetectLoop eventDetectLoop;

        /**
         * Event notify task.
         */
        private NotifyLoop notifyLoop;

        @Override
        public String toString() {
            return "Subscription{" +
                    "uuid='" + uuid + '\'' +
                    ", topic='" + topic + '\'' +
                    ", offset='" + offset + '\'' +
                    '}';
        }
    }

    /**
     * Notify task within unique thread.
     */
    @Data
    @EqualsAndHashCode(callSuper = true)
    class NotifyLoop extends StoppableTask {
        private String subscriptionId;
        private BlockingDeque<WeEvent> eventQueue;
        private IConsumer.ConsumerListener consumerListener;
        private Long notifyingEventCount = 0L;
        private Date notifyTimeStamp = new Date();

        private NotifyLoop(String subscriptionId, IConsumer.ConsumerListener consumerListener) {
            super("we-event-notify");
            this.subscriptionId = subscriptionId;
            this.eventQueue = new LinkedBlockingDeque<>();
            this.consumerListener = consumerListener;
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

                    this.notifyingEventCount++;
                    this.notifyTimeStamp = new Date();
                } catch (InterruptedException e) {
                    throw e;
                } catch (Exception e) {
                    this.consumerListener.onException(e);
                }
            }
        }
    }

    /**
     * Event loop task within unique thread.
     */
    class EventDetectLoop extends StoppableTask {
        /**
         * Lasted detected block.
         */
        private Long lastBlock = 0L;

        /**
         * Lasted received Event seq.
         */
        private Long lastEventSeq = 0L;

        /**
         * Cached value for ConsumerServiceImpl.getBlockHeight().
         */
        private Long cachedBlockHeight = 0L;

        private String topic;
        private NotifyLoop notifyLoop;

        public void setOffset(String offset) {
            switch (offset) {
                case WeEvent.OFFSET_FIRST:
                    this.lastBlock = 0L;
                    this.lastEventSeq = 0L;
                    break;

                case WeEvent.OFFSET_LAST:
                    this.lastBlock = -1L;
                    this.lastEventSeq = 0L;
                    break;

                default:
                    try {
                        Long lastEventSeq = DataTypeUtils.decodeSeq(offset);
                        Long lastBlock = DataTypeUtils.decodeBlockNumber(offset);
                        this.lastBlock = --lastBlock;
                        this.lastEventSeq = lastEventSeq;
                    } catch (BrokerException e) {
                        log.error("decodeSeq or decodeBlockNumber err:{}", e.getMessage());
                    }
            }
        }

        // /**
        // * Loop in helper thread if needed.
        // */
        // FasterLoopHelper fasterLoopHelper;

        EventDetectLoop(String topic, String offset, NotifyLoop notifyLoop) {
            super("we-event-loop");
            this.topic = topic;

            setOffset(offset);

            this.notifyLoop = notifyLoop;
        }

        void pushNotify(List<WeEvent> events) throws InterruptedException {
            // Skip event if needed.
            if (this.lastEventSeq > 0) {
                events.removeIf(node -> (getEventSeq(node.getEventId()) <= this.lastEventSeq));
            }

            for (WeEvent event : events) {
                if (!this.notifyLoop.eventQueue.offer(event, idleTime, TimeUnit.MILLISECONDS)) {
                    log.error("EventDetectLoop push notify failed due to queue full");
                    this.notifyLoop.consumerListener.onException(new BrokerException(
                            "onComplete is blocked too much time, notify queue full"));
                    return;
                }
                log.debug("offer notify queue, event: {}", event);
                // Ordered by event seq.
                this.lastEventSeq = getEventSeq(event.getEventId());
            }
        }

        @Override
        protected void taskOnceLoop() throws InterruptedException {
            try {
                // -1 Meanings last block while first loop enter.
                if (this.lastBlock == -1) {
                    Long blockHeight = topicService.getBlockHeight();
                    if (blockHeight <= 0) {
                        // Don't try too fast if net error.
                        idle();
                        return;
                    }
                    this.cachedBlockHeight = blockHeight;
                    this.lastBlock = blockHeight;
                    // Pull from next block.
                    return;
                }

                Long currentBlock = this.lastBlock + 1;
                // Cache may be expired, refresh it.
                if (currentBlock > this.cachedBlockHeight) {
                    Long blockHeight = topicService.getBlockHeight();
                    if (blockHeight <= 0) {
                        // Don't try too fast if net error.
                        idle();
                        return;
                    }
                    this.cachedBlockHeight = blockHeight;

                    // No new block.
                    if (currentBlock > this.cachedBlockHeight) {
                        log.debug("once loop, no new block, idle");
                        idle();
                        return;
                    }
                }

                // Normal loop one block as following.
                log.debug("once loop, topic: {} cached block height: {}", topic, this.cachedBlockHeight);
                List<WeEvent> events = loopBlock(currentBlock, topic);
                log.debug("once loop done, block: {} event size: {}", currentBlock, events.size());

                this.pushNotify(events);

                // Init for next block.
                this.lastBlock = currentBlock;
            } catch (BrokerException e) {
                this.notifyLoop.consumerListener.onException(e);
            }
        }
    }
}

