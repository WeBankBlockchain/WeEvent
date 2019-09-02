package com.webank.weevent.broker.task;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.webank.weevent.broker.fisco.util.DataTypeUtils;
import com.webank.weevent.broker.fisco.util.ParamCheckUtils;
import com.webank.weevent.broker.fisco.util.WeEventUtils;
import com.webank.weevent.broker.plugin.IConsumer;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.WeEvent;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * One topic subscription.
 * ##### more details #####
 * Normally, the events published in the future will be notified by MainEventLoop.
 * If the offset is not WeEvent.OFFSET_LAST, then it need a helper task HistoryEventLoop to fetch history events.
 * It works like as followings:
 * HistoryEventLoop(deal with events in caller thread if has in the same block of offset -> target history events -> current block) ===> MainEventLoop
 * HistoryEventLoop aim to fetch target event between offset and current block(current block will float forward while fetching).
 * It's notify task will switch into MainEventLoop while arriving at highest block height(=current block height in MainEventLoop).
 * But the switch action is done by MainEventLoop in another thread, HistoryEventLoop may float forward a few block at the same time.
 * Because once loop (both in HistoryEventLoop and MainEventLoop) is a long blocking task,
 * so we use a merge cache to avoid repeat notify, not a strict consistency. Like ideas showed in optimistic lock.
 *
 * @author matthewliu
 * @since 2019/08/30
 */
@Slf4j
@Getter
@Setter
public class Subscription {
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
    private String groupId;

    /**
     * event offset.
     */
    private String offset;

    /**
     * optional tag.
     */
    private String tag;

    /**
     * subscribe from which protocol, restful or json rpc, etc.
     */
    private String interfaceType;

    /**
     * subscribe from which ip
     */
    private String remoteIp;

    /**
     * idle time
     */
    private int idleTime = 1000;

    /**
     * merge cache block area
     */
    private int mergeBlock = 8;

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
    private Long historyBlock = 0L;

    /**
     * subscribe TimeStamp.
     */
    private Date subscribeTimeStamp = new Date();

    @Override
    public String toString() {
        return "Subscription{" +
                "uuid='" + this.uuid + '\'' +
                ", topic='" + Arrays.toString(this.topics) + '\'' +
                ", groupId='" + this.groupId + '\'' +
                ", offset='" + this.offset + '\'' +
                ", tag='" + this.tag + '\'' +
                '}';
    }

    public Subscription(IBlockChain blockChain, String uuid, String[] topics, String groupId, String offset, String tag, IConsumer.ConsumerListener listener) throws BrokerException {
        if (StringUtils.isBlank(uuid)) {
            this.uuid = UUID.randomUUID().toString();
        } else {
            this.uuid = uuid;
        }

        this.topics = topics;
        this.groupId = groupId;
        this.offset = offset;
        this.tag = tag;

        this.notifyTask = new NotifyTask(this.uuid, blockChain.getIdleTime(), listener);

        // not OFFSET_LAST, need history help task
        if (!WeEvent.OFFSET_LAST.equals(this.offset)) {
            log.info("need history event loop, {}", this);

            Long lastBlock;
            if (WeEvent.OFFSET_LAST.equals(this.offset)) {
                lastBlock = 0L;
            } else {
                lastBlock = DataTypeUtils.decodeBlockNumber(offset);
            }
            this.historyEventLoop = new HistoryEventLoop(blockChain, this, lastBlock);

            this.historyBlock = this.historyEventLoop.getLastBlock();
            this.mergeCache = new HashMap<>();
        }
    }

    public Long getNotifyingEventCount() {
        return (long) this.notifyTask.getEventQueue().size();
    }

    public Long getNotifiedEventCount() {
        return this.notifyTask.getNotifiedCount();
    }

    public Date getNotifyTimeStamp() {
        return this.notifyTask.getLastTimeStamp();
    }

    // mainLoop = true meanings dispatch from MainEventLoop, always history first then main loop
    public void dispatch(List<WeEvent> events, boolean mainLoop, Long blockHeight) {
        // skip block before notified history block
        if (blockHeight <= this.historyBlock) {
            return;
        }

        List<WeEvent> topicEvents;
        if (mainLoop) {
            // filter the events if from main loop
            topicEvents = filter(events, this.topics, this.tag);
            if (topicEvents.isEmpty()) {
                return;
            }
        } else {
            log.info("dispatch from HistoryEventLoop");

            // record last history block
            if (blockHeight > this.historyBlock) {
                this.historyBlock = blockHeight;
            }

            // already filter if from history
            topicEvents = events;
        }

        // need merge in cache
        if (this.mergeCache != null) {
            topicEvents.removeIf((event) -> this.mergeCache.containsKey(event.getEventId()));
            for (WeEvent event : topicEvents) {
                this.mergeCache.put(event.getEventId(), event);
            }

            // cleanup merge cache if needed
            if (mainLoop && blockHeight > this.historyBlock + this.mergeBlock) {
                log.info("HistoryEventLoop finalize merge cache at block: {}", this.mergeBlock);

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

    public synchronized void doStart(ThreadPoolTaskExecutor threadPoolTaskExecutor) {
        threadPoolTaskExecutor.execute(this.notifyTask);
        if (this.historyEventLoop != null) {
            threadPoolTaskExecutor.execute(this.historyEventLoop);
        }
    }

    // can not doStart again after doStop
    public synchronized void doStop() {
        this.notifyTask.doExit();

        // wait task exit really
        StoppableTask.idle(this.idleTime);

        stopHistory();
    }

    public synchronized void stopHistory() {
        if (this.historyEventLoop != null) {
            this.historyEventLoop.doExit();
            this.historyEventLoop = null;
        }
    }

    public synchronized boolean tryStopHistory(Long mainLastBlock) {
        if (mainLastBlock > 0 && mainLastBlock <= this.historyEventLoop.getLastBlock()) {
            log.info("switch history to main event loop, {} ---> {}, {}",
                    this.historyEventLoop.getLastBlock(), mainLastBlock, this);

            stopHistory();
            return true;
        }

        return false;
    }

    /**
     * filter event with topic name or pattern
     *
     * @param from original event list
     * @param topics topic name or Pattern list
     * @return target event list
     */
    public static List<WeEvent> filter(List<WeEvent> from, String[] topics, String tag) {
        List<WeEvent> to = new ArrayList<>();
        for (WeEvent event : from) {
            for (String topic : topics) {
                boolean topic_target = false;
                // subscription in pattern
                if (ParamCheckUtils.isTopicPattern(topic)) {
                    if (WeEventUtils.match(event.getTopic(), topic)) {
                        topic_target = true;
                    }
                } else { // subscription in normal topic
                    if (topic.equals(event.getTopic())) {
                        topic_target = true;
                    }
                }

                if (topic_target) {
                    // subscription without tag
                    if (StringUtils.isBlank(tag)) {
                        to.add(event);
                    } else {    // subscription in tag plus
                        if (event.getExtensions() != null
                                && tag.equals(event.getExtensions().get(WeEvent.WeEvent_TAG))) {
                            to.add(event);
                        }
                    }
                }
            }
        }

        return to;
    }
}
