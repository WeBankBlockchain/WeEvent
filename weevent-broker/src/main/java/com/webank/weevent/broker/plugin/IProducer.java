package com.webank.weevent.broker.plugin;


import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.webank.weevent.broker.fisco.web3sdk.FiscoBcosDelegate;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.SendResult;
import com.webank.weevent.sdk.WeEvent;

/**
 * Base interface for event producer.
 *
 * @author matthewliu
 * @since 2018/11/02
 */
public interface IProducer extends IEventTopic {

    /**
     * Start a producer channel.
     *
     * @return boolean true if success
     * @throws BrokerException BrokerException
     */
    boolean startProducer() throws BrokerException;

    /**
     * Shutdown a producer channel.
     *
     * @return boolean true if success
     */
    boolean shutdownProducer();

    /**
     * Publish a event in asynchronous way.
     *
     * @param event the event
     * @param groupId group id
     * @return SendResult SendResult
     * @throws BrokerException BrokerException
     */
    CompletableFuture<SendResult> publish(WeEvent event, String groupId) throws BrokerException;

    /**
     * Publish a event in synchronize way
     *
     * @param event the event
     * @param groupId group id
     * @return SendResult SendResult
     * @throws BrokerException BrokerException
     */
    default SendResult publishSync(WeEvent event, String groupId) throws BrokerException {
        try {
            return this.publish(event, groupId).get(FiscoBcosDelegate.timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException e) {
            SendResult sendResult = new SendResult(SendResult.SendResultStatus.ERROR);
            sendResult.setTopic(event.getTopic());
            return sendResult;
        } catch (TimeoutException e) {
            SendResult sendResult = new SendResult(SendResult.SendResultStatus.TIMEOUT);
            sendResult.setTopic(event.getTopic());
            return sendResult;
        }
    }
}
