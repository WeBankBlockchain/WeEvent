package com.webank.weevent.broker.fisco.util;


import lombok.extern.slf4j.Slf4j;

/**
 * Task can be stopped by exit flag and interrupt().
 * Prefer to exit flag.
 *
 * @author matthewliu
 * @since 2018/11/09
 */
@Slf4j
public abstract class StoppableTask extends Thread {
    // flag to exit task
    private volatile boolean exit = false;

    public StoppableTask(String name) {
        super(name);
    }

    public void doExit() {
        log.info("enter");

        this.exit = true;

        for (int i = 0; i < 15; i++) {
            try {
                log.debug("try to graceful exit");
                // idle 1 second
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                log.error("exit failed, due to InterruptedException");
                Thread.currentThread().interrupt();
            }

            if (!isAlive()) {
                log.info("not alive, doExit carefully");
                return;
            }
        }

        // Force terminate, Web3sdk will throw NullPointException if interrupt()
        //this.interrupt();
        log.info("force to exit after wait");
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

        log.info("task thread exit, name: {}", this.getName());
    }
}
