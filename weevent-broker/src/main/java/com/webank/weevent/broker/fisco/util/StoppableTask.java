package com.webank.weevent.broker.fisco.util;


import com.webank.weevent.BrokerApplication;

import lombok.extern.slf4j.Slf4j;

/**
 * Task can be stopped by flag setting and InterruptedException.
 * Prefer to flag setting.
 *
 * @author matthewliu
 * @since 2018/11/09
 */
@Slf4j
public abstract class StoppableTask extends Thread {
    private volatile boolean exit = false;

    public StoppableTask(String name) {
        super(name);
    }

    public boolean isExit() {
        return this.exit;
    }

    public void doExit() {
        log.info("doExit enter");

        this.exit = true;

        int idleTime = BrokerApplication.weEventConfig.getConsumerIdleTime();
        for (int i = 0; i < 15; i++) {
            try {
                log.debug("idle to graceful exit");
                Thread.sleep(idleTime);
            } catch (InterruptedException e) {
                log.error("doExit failed, due to InterruptedException");
                Thread.currentThread().interrupt();
            }

            if (!isAlive()) {
                log.info("doExit exit while not alive");
                return;
            }
        }

        // Force terminate, Web3sdk will throw NullPointException if interrupt()
        //this.interrupt();
        log.info("doExit force exit after wait");
    }

    private void onExit() {
    }

    protected abstract void taskOnceLoop() throws InterruptedException;

    @Override
    public void run() {
        log.info("task thread enter, name: {}", this.getName());

        try {
            while (!this.exit) {
                if (isInterrupted()) {
                    break;
                }
                this.taskOnceLoop();
            }
        } catch (InterruptedException e) {
            log.info("catch InterruptedException to exit.");
            Thread.currentThread().interrupt();
        }

        this.onExit();
        log.info("task thread exit, name: {}", this.getName());
    }
}
