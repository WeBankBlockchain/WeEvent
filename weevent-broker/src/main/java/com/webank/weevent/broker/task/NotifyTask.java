package com.webank.weevent.broker.task;


import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import com.webank.weevent.broker.plugin.IConsumer;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.ErrorCode;
import com.webank.weevent.sdk.WeEvent;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * Notify task run in unique thread.
 *
 * @author matthewliu
 * @since 2019/08/30
 */
@Slf4j
@Getter
public class NotifyTask extends StoppableTask {
    private String subscriptionId;
    private IConsumer.ConsumerListener consumerListener;

    private int idleTime;
    private BlockingDeque<WeEvent> eventQueue;
    private long notifiedCount = 0;
    private Date lastTimeStamp;

    public NotifyTask(String subscriptionId, int idleTime, @NonNull IConsumer.ConsumerListener consumerListener) {
        super("event-notify@" + subscriptionId);

        this.subscriptionId = subscriptionId;
        this.consumerListener = consumerListener;

        this.idleTime = idleTime;
        this.eventQueue = new LinkedBlockingDeque<>();
        this.lastTimeStamp = new Date();
    }

    public void push(List<WeEvent> events) {
        try {
            // offer event into queue one by one
            for (WeEvent event : events) {
                if (!this.eventQueue.offer(event, this.idleTime, TimeUnit.MILLISECONDS)) {
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
            WeEvent event = this.eventQueue.poll(this.idleTime, TimeUnit.MILLISECONDS);
            // Empty queue, try next.
            if (event == null) {
                return;
            }
            log.debug("poll from notify queue, event: {}", event);

            // call back once for a single event
            this.consumerListener.onEvent(this.subscriptionId, event);
            this.notifiedCount++;
            this.lastTimeStamp.setTime(System.currentTimeMillis());

            log.info("notify biz done, subscriptionId: {} eventId: {}", this.subscriptionId, event.getEventId());
        } catch (Exception e) {
            this.consumerListener.onException(e);
        }
    }
}
