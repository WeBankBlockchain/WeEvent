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
public abstract class StoppableTask implements Runnable {
    private String name;

    // flag to exit task
    private volatile boolean exit = false;

    public StoppableTask(String name) {
        this.name = name;
    }

    public void doExit() {
        log.info("set exit flag, name: {}", this.name);
        this.exit = true;
    }

    protected abstract void taskOnceLoop() throws InterruptedException;

    @Override
    public void run() {
        log.info("task enter, name: {}", this.name);

        try {
            while (!this.exit) {
                this.taskOnceLoop();
            }
        } catch (Exception e) {
            log.error("Exception in task, name: " + this.name, e);
        }

        log.info("task exit, name: {}", this.name);
    }
}
