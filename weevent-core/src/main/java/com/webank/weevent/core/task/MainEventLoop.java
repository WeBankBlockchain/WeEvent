package com.webank.weevent.core.task;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import com.webank.weevent.client.BrokerException;
import com.webank.weevent.client.ErrorCode;
import com.webank.weevent.client.WeEvent;

import lombok.extern.slf4j.Slf4j;

/**
 * Detect new event from target group.
 *
 * @author matthewliu
 * @since 2019/08/30
 */
@Slf4j
public class MainEventLoop extends StoppableTask {
    // daemon Executor
    private final Executor executor;

    // block chain
    private final IBlockChain blockChain;

    // binding group
    private final String groupId;

    // last well done block
    private Long lastBlock;

    // Subscription ID <-> Subscription
    private final Map<String, Subscription> subscriptions = new ConcurrentHashMap<>();

    // subscription in main loop
    private final List<String> mainSubscriptionIds = new ArrayList<>();

    // subscription in history loop
    private final List<String> historySubscriptionIds = new ArrayList<>();

    // new block notified
    private BlockingDeque<Long> blockNotifyQueue;

    public MainEventLoop(Executor executor, IBlockChain blockChain, String groupId) throws BrokerException {
        super("main-event-loop-" + groupId);
        this.executor = executor;
        this.blockChain = blockChain;
        this.groupId = groupId;

        // get last block
        Long blockHeight = this.blockChain.getBlockHeight(this.groupId);
        if (blockHeight <= 0) {
            throw new BrokerException(ErrorCode.WEB3SDK_INIT_ERROR);
        }

        // may be in notify strategy
        if (blockChain.hasBlockEventNotify()) {
            this.blockNotifyQueue = new LinkedBlockingDeque<>();
        }

        // init last block
        this.lastBlock = blockHeight;
        log.info("MainEventLoop initialized with last block: {} in group: {}", this.lastBlock, this.groupId);
    }

    public synchronized void doStart() {
        this.executor.execute(this);
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
        subscription.doStart(this.executor);

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
            if (this.blockNotifyQueue == null) {
                this.normalLoop();
            } else {
                this.loopWithNotifySupport();
            }
        } catch (BrokerException e) {
            log.error("main event loop exception in group: " + this.groupId, e);
            this.dispatch(e);
        }
    }

    /**
     * normal loop
     *
     * @throws BrokerException BrokerException
     */
    private void normalLoop() throws BrokerException {
        // the block try to deal with in this one loop
        Long currentBlock = this.lastBlock + 1;

        // get block height
        Long blockHeight = this.blockChain.getBlockHeight(this.groupId);
        if (blockHeight <= 0) {
            log.error("get block height failed, retry");

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

        this.dealOneBlock(currentBlock);
    }

    public void onNewBlock(Long blockHeight) {
        log.info("new block event from web3sdk, {}", blockHeight);

        try {
            if (!this.blockNotifyQueue.offer(blockHeight, this.blockChain.getIdleTime(), TimeUnit.MILLISECONDS)) {
                log.error("new block event queue failed due to queue is full");
            }
        } catch (InterruptedException e) {
            log.error("new block event queue failed", e);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * loop with new block event notify support, like in FISCO-BCOS 2.0
     *
     * @throws BrokerException BrokerException
     */
    private void loopWithNotifySupport() throws BrokerException {
        // the block try to deal with in this one loop
        Long currentBlock = this.lastBlock + 1;

        // get block height
        Long blockHeight;
        try {
            blockHeight = this.blockNotifyQueue.poll(this.blockChain.getIdleTime(), TimeUnit.MILLISECONDS);
            if (blockHeight == null) {
                log.debug("can not find new block event notify within idle time");

                blockHeight = this.blockChain.getBlockHeight(this.groupId);
                if (blockHeight <= 0) {
                    log.error("get block height failed, retry");
                    // retry if net error.
                    return;
                }
            }
        } catch (Exception e) {
            log.error("get notify from new block event queue failed", e);
            return;
        }

        // retry if no new block
        if (currentBlock > blockHeight) {
            log.debug("no new block in group: {}, idle", this.groupId);
            return;
        }

        this.dealOneBlock(currentBlock);
    }

    private void dealOneBlock(Long currentBlock) throws BrokerException {
        // merge history if needed
        if (!this.historySubscriptionIds.isEmpty()) {
            this.mergeHistory();
        }

        // no need to fetch event if no subscription
        if (!this.mainSubscriptionIds.isEmpty()) {
            // fetch all event from block chain in this block
            List<WeEvent> events = this.blockChain.loop(currentBlock, this.groupId);
            // idle until get event information(include empty)
            if (events == null) {
                log.error("fetch events from block failed, block height: {}", currentBlock);
                StoppableTask.idle(this.blockChain.getIdleTime());
                return;
            }
            log.info("fetch events done, block: {} group: {} event size: {}", currentBlock, this.groupId, events.size());

            this.dispatch(events, currentBlock);
        }

        // next block
        this.lastBlock = currentBlock;
    }
}
