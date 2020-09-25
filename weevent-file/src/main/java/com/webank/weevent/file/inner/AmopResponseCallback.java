package com.webank.weevent.file.inner;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.sdk.channel.ResponseCallback;
import org.fisco.bcos.sdk.model.Response;

@Slf4j
public class AmopResponseCallback extends ResponseCallback implements Future<Response> {

    private final CountDownLatch latch = new CountDownLatch(1);
    private Response rsp;

    @Override
    public void onResponse(Response response) {
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

    public Response get() throws InterruptedException {
        this.latch.await();
        return this.rsp;
    }

    @Override
    public Response get(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
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
