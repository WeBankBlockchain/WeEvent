package com.webank.weevent.core.task;


import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import com.webank.weevent.client.BrokerException;
import com.webank.weevent.client.ErrorCode;
import com.webank.weevent.client.WeEvent;
import com.webank.weevent.core.IConsumer;

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
    private final String subscriptionId;
    private final IConsumer.ConsumerListener consumerListener;
    private final int idleTime;

    private final BlockingDeque<WeEvent> eventQueue = new LinkedBlockingDeque<>();
    private long notifiedCount = 0;
    private final Date lastTimeStamp = new Date();

    // (eventId <-> timestamp), value is not used yet
    private final Map<String, Long> mergeCache = new FixedFIFOCache<>(1024);

    // fixed size FIFO cache
    static class FixedFIFOCache<K, V> extends LinkedHashMap<K, V> {
        private final int capacity;

        public FixedFIFOCache(int capacity) {
            super(capacity, 0.8f, false);
            this.capacity = capacity;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return this.size() > capacity;
        }
    }

    public NotifyTask(String subscriptionId, int idleTime, @NonNull IConsumer.ConsumerListener consumerListener) {
        super("event-notify@" + subscriptionId);

        this.subscriptionId = subscriptionId;
        this.consumerListener = consumerListener;
        this.idleTime = idleTime;
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
            Thread.currentThread().interrupt();
        }
    }

    @Override
    protected void taskOnceLoop() {
        try {
            WeEvent event = this.eventQueue.poll(this.idleTime, TimeUnit.MILLISECONDS);
            // empty queue, try next.
            if (event == null) {
                return;
            }
            log.debug("poll from notify queue, event: {}", event);

            // avoid duplicate with FIFO cache
            if (this.mergeCache.containsKey(event.getEventId())) {
                log.warn("event to be notify again, skip {}", event.getEventId());
            } else {
                // notify a single event once time
                this.consumerListener.onEvent(this.subscriptionId, event);
                this.notifiedCount++;
                long now = System.currentTimeMillis();
                this.lastTimeStamp.setTime(now);

                // avoid duplicate with FIFO cache
                this.mergeCache.put(event.getEventId(), now);

                log.info("notify biz done, subscriptionId: {} eventId: {}", this.subscriptionId, event.getEventId());
            }
        } catch (Exception e) {
            this.consumerListener.onException(e);
        }
    }
}
