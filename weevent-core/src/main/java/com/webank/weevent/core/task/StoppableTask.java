package com.webank.weevent.core.task;

import lombok.extern.slf4j.Slf4j;

/**
 * Task that can be stopped by exit flag.
 *
 * @author matthewliu
 * @since 2018/11/09
 */
@Slf4j
public abstract class StoppableTask implements Runnable {
    private final String name;

    // flag to exit task
    private volatile boolean exit = false;

    public StoppableTask(String name) {
        this.name = name;
    }

    /*
     * Idle the caller thread some time
     */
    public static void idle(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            log.warn("got InterruptedException in idle");
            Thread.currentThread().interrupt();
        }
    }

    public void doExit() {
        log.info("set exit flag, name: {}", this.name);
        this.exit = true;
    }

    // task's real do loop
    protected abstract void taskOnceLoop();

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
