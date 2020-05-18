package com.webank.weevent.file.inner;


import com.webank.weevent.client.WeEvent;

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
