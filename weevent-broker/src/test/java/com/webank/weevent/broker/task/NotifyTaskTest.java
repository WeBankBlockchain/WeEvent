package com.webank.weevent.broker.task;

import java.util.ArrayList;
import java.util.List;

import com.webank.weevent.JUnitTestBase;
import com.webank.weevent.broker.plugin.IConsumer;
import com.webank.weevent.sdk.WeEvent;

import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * NotifyTask Tester.
 *
 * @author matthewliu
 * @version 1.0
 * @since 09/02/2019
 */
@Slf4j
public class NotifyTaskTest extends JUnitTestBase {
    private String subscriptionId = "abc";
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    private final long wait3s = 3000;

    static class MyListener implements IConsumer.ConsumerListener {
        public long received = 0;
        public String subscriptionId;

        @Override
        public void onEvent(String subscriptionId, WeEvent event) {
            this.subscriptionId = subscriptionId;
            received++;
        }

        @Override
        public void onException(Throwable e) {
            received = -10000;
        }
    }

    ;

    @Before
    public void before() {
        this.threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        this.threadPoolTaskExecutor.initialize();
    }

    @After
    public void after() {
        this.threadPoolTaskExecutor.destroy();
    }

    /**
     * Method: push(List<WeEvent> events)
     */
    @Test
    public void testPush() throws Exception {
        log.info("===================={}", this.testName.getMethodName());

        MyListener listener = new MyListener();
        NotifyTask notifyTask = new NotifyTask(this.subscriptionId, 1000, listener);
        this.threadPoolTaskExecutor.execute(notifyTask);

        List<WeEvent> data = new ArrayList<>();
        data.add(new WeEvent());

        notifyTask.push(data);
        Thread.sleep(wait3s);

        Assert.assertEquals(listener.received, 1);
        Assert.assertEquals(subscriptionId, listener.subscriptionId);
        Assert.assertEquals(notifyTask.getNotifiedCount(), listener.received);
    }

    /**
     * Method: push(List<WeEvent> events)
     */
    @Test
    public void testPush2() throws Exception {
        log.info("===================={}", this.testName.getMethodName());

        MyListener listener = new MyListener();
        NotifyTask notifyTask = new NotifyTask(this.subscriptionId, 1000, listener);
        this.threadPoolTaskExecutor.execute(notifyTask);

        List<WeEvent> data = new ArrayList<>();
        data.add(new WeEvent());
        data.add(new WeEvent());

        notifyTask.push(data);
        Thread.sleep(wait3s);

        Assert.assertEquals(listener.received, 2);
        Assert.assertEquals(subscriptionId, listener.subscriptionId);
        Assert.assertEquals(notifyTask.getNotifiedCount(), listener.received);
    }

    /**
     * Method: push(List<WeEvent> events)
     */
    @Test
    public void testPush3() throws Exception {
        log.info("===================={}", this.testName.getMethodName());

        MyListener listener = new MyListener();
        NotifyTask notifyTask = new NotifyTask(this.subscriptionId, 1000, listener);
        this.threadPoolTaskExecutor.execute(notifyTask);

        List<WeEvent> data = new ArrayList<>();
        data.add(new WeEvent());
        data.add(new WeEvent());
        notifyTask.push(data);
        notifyTask.push(data);
        notifyTask.push(data);
        Thread.sleep(wait3s);

        Assert.assertEquals(listener.received, 6);
        Assert.assertEquals(subscriptionId, listener.subscriptionId);
        Assert.assertEquals(notifyTask.getNotifiedCount(), listener.received);
    }
} 
