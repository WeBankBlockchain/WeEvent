package com.webank.weevent.broker.task;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.ErrorCode;
import com.webank.weevent.sdk.WeEvent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Detect new event from target group.
 *
 * @author matthewliu
 * @since 2019/08/30
 */
@Slf4j
public class MainEventLoop extends StoppableTask {
    // daemon thread pool
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    // block chain
    private IBlockChain blockChain;

    // binding group
    private String groupId;

    // last well done block
    private Long lastBlock;

    // Subscription ID <-> Subscription
    private Map<String, Subscription> subscriptions = new ConcurrentHashMap<>();

    // subscription in main loop
    private List<String> mainSubscriptionIds = new ArrayList<>();

    // subscription in history loop
    private List<String> historySubscriptionIds = new ArrayList<>();

    public MainEventLoop(ThreadPoolTaskExecutor threadPoolTaskExecutor, IBlockChain blockChain, String groupId) throws BrokerException {
        super("main-event-loop-" + groupId);
        this.threadPoolTaskExecutor = threadPoolTaskExecutor;
        this.blockChain = blockChain;
        this.groupId = groupId;

        // get last block
        Long blockHeight = this.blockChain.getBlockHeight(this.groupId);
        if (blockHeight <= 0) {
            throw new BrokerException(ErrorCode.WE3SDK_INIT_ERROR);
        }

        // init last block
        this.lastBlock = blockHeight;
        log.info("MainEventLoop initialized with last block: {} in group: {}", this.lastBlock, this.groupId);
    }

    public synchronized void doStart() {
        this.threadPoolTaskExecutor.execute(this);
    }

    public synchronized void doStop() {
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

    public synchronized void addSubscription(Subscription subscription) {
        subscription.doStart(this.threadPoolTaskExecutor);

        this.subscriptions.put(subscription.getUuid(), subscription);
        if (subscription.getHistoryEventLoop() == null) {
            this.mainSubscriptionIds.add(subscription.getUuid());
        } else {
            this.historySubscriptionIds.add(subscription.getUuid());
        }
    }

    public synchronized void removeSubscription(Subscription subscription) {
        if (subscription.getHistoryEventLoop() == null) {
            this.mainSubscriptionIds.remove(subscription.getUuid());
        } else {
            this.historySubscriptionIds.remove(subscription.getUuid());
        }

        subscription.doStop();
        this.subscriptions.remove(subscription.getUuid());
    }

    private synchronized void mergeHistory() {
        List<String> stopped = new ArrayList<>();
        for (String subscriptionId : this.historySubscriptionIds) {
            if (this.subscriptions.containsKey(subscriptionId)
                    && this.subscriptions.get(subscriptionId).tryStopHistory(this.lastBlock)) {
                // try to stop history if needed
                stopped.add(subscriptionId);
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
            this.subscriptions.get(subscriptionId).dispatch(events, true, blockHeight);
        }
    }

    private synchronized void dispatch(Throwable e) {
        for (String subscriptionId : this.mainSubscriptionIds) {
            this.subscriptions.get(subscriptionId).getNotifyTask().getConsumerListener().onException(e);
        }
    }

    @Override
    protected void taskOnceLoop() {
        try {
            // the block try to deal with in this one loop
            Long currentBlock = this.lastBlock + 1;

            // get block height
            Long blockHeight = this.blockChain.getBlockHeight(this.groupId);
            if (blockHeight <= 0) {
                // Don't try too fast if net error.
                StoppableTask.idle(this.blockChain.getIdleTime());
                return;
            }
            // no new block
            if (currentBlock > blockHeight) {
                log.debug("no new block in group: {}, idle", this.groupId);
                StoppableTask.idle(this.blockChain.getIdleTime());
                return;
            }

            // merge history if needed
            this.mergeHistory();

            // no need to fetch event if no subscription
            if (!this.mainSubscriptionIds.isEmpty()) {
                // fetch all event from block chain in this block
                log.debug("fetch events from block height: {} in group: {}", currentBlock, this.groupId);
                List<WeEvent> events = this.blockChain.loop(currentBlock, this.groupId);
                // idle until get event information(include empty)
                if (events == null) {
                    StoppableTask.idle(this.blockChain.getIdleTime());
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
