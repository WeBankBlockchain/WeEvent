package com.webank.weevent.broker.fisco.file;


import com.webank.weevent.sdk.WeEvent;

/**
 * Notify WeEvent to remote.
 *
 * @author matthewliu
 * @since 2020/02/16
 */
public interface NotifyWeEvent {
    // see BrokerStomp.handleOnEvent
    void send(String subscriptionId, WeEvent event);
}
