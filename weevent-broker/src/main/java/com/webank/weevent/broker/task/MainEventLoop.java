package com.webank.weevent.broker.task;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

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

    // new block notified
    private BlockingDeque<Long> blockNotifyQueue;

    // last notified timestamp
    private long lastNotifiedTimeStamp;

    // faked notify task
    private StoppableTask fakedNotify;

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

        // may be in notify strategy
        if (blockChain.hasBlockEventNotify()) {
            this.blockNotifyQueue = new LinkedBlockingDeque<>();

            // try to fail over block chain's notify fault with faked(very very few, and scarce)
            this.fakedNotify = new StoppableTask("faked-notify-" + groupId) {
                @Override
                protected void taskOnceLoop() {
                    StoppableTask.idle(blockChain.getIdleTime());

                    // need a faked notify
                    if (lastNotifiedTimeStamp + blockChain.getIdleTime() < System.currentTimeMillis()) {
                        onNewBlock(0L);
                    }
                }
            };
        }

        // init last block
        this.lastBlock = blockHeight;
        this.lastNotifiedTimeStamp = System.currentTimeMillis();
        log.info("MainEventLoop initialized with last block: {} in group: {}", this.lastBlock, this.groupId);
    }

    public synchronized void doStart() {
        this.threadPoolTaskExecutor.execute(this);

        if (this.fakedNotify != null) {
            this.threadPoolTaskExecutor.execute(this.fakedNotify);
        }
    }

    public synchronized void doStop() {
        log.info("try to stop MainEventLoop in group: {}", this.groupId);

        if (this.fakedNotify != null) {
            this.fakedNotify.doExit();
        }

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
     * normal loop, like in FISCO-BCOS 1.3
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
        log.debug("new block event from web3sdk, {}(0 meanings faked notify)", blockHeight);

        try {
            if (this.blockNotifyQueue.offer(blockHeight, this.blockChain.getIdleTime(), TimeUnit.MILLISECONDS)) {
                this.lastNotifiedTimeStamp = System.currentTimeMillis();
            } else {
                log.error("new block event queue failed due to queue is full");
            }
        } catch (InterruptedException e) {
            log.error("new block event queue failed", e);
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
        Long notify;
        try {
            notify = this.blockNotifyQueue.poll(this.blockChain.getIdleTime(), TimeUnit.MILLISECONDS);
            if (notify == null) {
                log.error("can not find new block event notify(included faked)");
                notify = 0L;
            }
        } catch (Exception e) {
            log.error("get notify from new block event queue failed", e);
            notify = 0L;
        }

        Long blockHeight = notify;
        // fail over block chain's notify fault
        if (notify == 0L) {
            blockHeight = this.blockChain.getBlockHeight(this.groupId);
            if (blockHeight <= 0) {
                log.error("get block height failed, retry");
                // retry if net error.
                return;
            }
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
        this.mergeHistory();

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
