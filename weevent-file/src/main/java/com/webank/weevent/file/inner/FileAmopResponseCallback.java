package com.webank.weevent.file.inner;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.sdk.amop.AmopResponse;
import org.fisco.bcos.sdk.amop.AmopResponseCallback;
import org.fisco.bcos.sdk.model.Response;

@Slf4j
public class FileAmopResponseCallback extends AmopResponseCallback implements Future<AmopResponse> {

    private final CountDownLatch latch = new CountDownLatch(1);
    private AmopResponse rsp;

    @Override
    public void onResponse(AmopResponse response) {
        this.rsp = response;
        this.latch.countDown();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        //not support
        return false;
    }

    @Override
    public boolean isCancelled() {
        //not support
        return false;
    }

    @Override
    public boolean isDone() {
        return this.latch.getCount() == 0;
    }

    public AmopResponse get() throws InterruptedException {
        this.latch.await();
        return this.rsp;
    }

    @Override
    public AmopResponse get(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        if (latch.await(timeout, unit)) {
            if (this.rsp != null) {
                return this.rsp;
            } else {
                log.error("empty response");
                throw new TimeoutException();
            }
        } else {
            log.error("empty response");
            throw new TimeoutException();
        }
    }
}
