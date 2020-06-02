package com.webank.weevent.core.task;


import java.util.Arrays;
import java.util.List;

import com.webank.weevent.client.BrokerException;
import com.webank.weevent.client.ErrorCode;
import com.webank.weevent.client.WeEvent;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * History event loop task within unique thread.
 *
 * @author matthewliu
 * @since 2019/08/30
 */
@Slf4j
public class HistoryEventLoop extends StoppableTask {
    /**
     * block chain
     */
    private final IBlockChain blockChain;

    /**
     * Last detected block.
     */
    private Long lastBlock;

    /**
     * Cached value for highest block height.
     */
    private Long cachedBlockHeight = 0L;

    /**
     * binding to dispatch event
     */
    private final Subscription subscription;

    public Long getLastBlock() {
        return this.lastBlock;
    }

    public HistoryEventLoop(IBlockChain blockChain, Subscription subscription, Long lastBlock) throws BrokerException {
        super("history-event-loop@" + subscription.getUuid());
        this.blockChain = blockChain;
        this.subscription = subscription;

        // if offset is block height, filter next block directly
        if (lastBlock != 0 && !StringUtils.isNumeric(this.subscription.getOffset())) {
            this.dispatchTargetBlock(lastBlock, this.subscription.getOffset());
        }
        this.lastBlock = lastBlock;

        log.info("HistoryEventLoop initialized with last block: {}, {}", this.lastBlock, this.subscription);
    }

    /**
     * dispatch the target block where the offset is directly in caller thead.
     * dispatch in caller thread to make sure offset/eventId is exist.
     *
     * @param blockNum block num
     */
    private void dispatchTargetBlock(Long blockNum, String offset) throws BrokerException {
        // fetch event from block chain
        List<WeEvent> events = filterBlockEvent(blockNum,
                this.subscription.getTopics(),
                this.subscription.getGroupId(),
                this.subscription.getTag());
        log.info("fetch events from block height: {} topics: {} tag: {} events num: {}",
                blockNum,
                Arrays.toString(this.subscription.getTopics()),
                this.subscription.getTag(),
                events.size());

        int targetIdx = -1;
        for (int idx = 0; idx < events.size(); idx++) {
            if (events.get(idx).getEventId().equals(offset)) {
                targetIdx = idx;
                break;
            }
        }

        // can not find offset
        if (targetIdx < 0) {
            log.error("can not find eventId: {} in block height: {}", offset, blockNum);
            throw new BrokerException(ErrorCode.EVENT_ID_NOT_EXIST);
        }

        // get event after offset, exclusive offset itself
        events = events.subList(targetIdx + 1, events.size());

        // dispatch event after offset
        if (!events.isEmpty()) {
            this.subscription.dispatch(events, false, blockNum);
        }
    }

    /**
     * filter event from one block
     *
     * @param blockNum block height
     * @param topics topic list
     * @param groupId groupId
     * @param tag tag
     * @return event list
     * @throws BrokerException the exp
     */
    private List<WeEvent> filterBlockEvent(Long blockNum, String[] topics, String groupId, String tag) throws BrokerException {
        // idle until get event
        List<WeEvent> blockEventsList = null;
        while (blockEventsList == null) {
            blockEventsList = this.blockChain.loop(blockNum, groupId);
            if (blockEventsList == null) {
                StoppableTask.idle(this.blockChain.getIdleTime());
            }
        }

        // filter target event
        return Subscription.filter(blockEventsList, topics, tag);
    }

    @Override
    protected void taskOnceLoop() {
        try {
            // current block height to deal with in this one loop
            Long currentBlock = this.lastBlock + 1;

            // cache may be expired, refresh it
            if (currentBlock > this.cachedBlockHeight) {
                Long blockHeight = this.blockChain.getBlockHeight(this.subscription.getGroupId());
                if (blockHeight <= 0) {
                    // Don't try too fast if net error.
                    StoppableTask.idle(this.blockChain.getIdleTime());
                    return;
                }
                this.cachedBlockHeight = blockHeight;

                // no new block
                if (currentBlock > this.cachedBlockHeight) {
                    log.debug("no new block in group: {}, idle", this.subscription.getGroupId());
                    StoppableTask.idle(this.blockChain.getIdleTime());
                    return;
                }
            }

            // loop one block, and filter target event with topic
            log.debug("history event loop in group: {}, topics: {} cached block height: {}",
                    this.subscription.getGroupId(),
                    Arrays.toString(this.subscription.getTopics()),
                    this.cachedBlockHeight);
            List<WeEvent> events = filterBlockEvent(currentBlock,
                    this.subscription.getTopics(),
                    this.subscription.getGroupId(),
                    this.subscription.getTag());
            log.debug("history event loop done, block: {} event size: {}", currentBlock, events.size());

            // it there is event to notify
            if (!events.isEmpty()) {
                this.subscription.dispatch(events, false, currentBlock);
            }

            // next block
            this.lastBlock = currentBlock;
        } catch (BrokerException e) {
            log.error("history event loop exception in group: " + this.subscription.getGroupId(), e);
            this.subscription.getNotifyTask().getConsumerListener().onException(e);
        }
    }
}
