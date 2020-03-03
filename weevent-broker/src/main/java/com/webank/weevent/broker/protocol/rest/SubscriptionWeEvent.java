package com.webank.weevent.broker.protocol.rest;


import com.webank.weevent.client.WeEvent;

import lombok.Getter;
import lombok.Setter;

/**
 * WeEvent with subscription ID.
 *
 * @author matthewliu
 * @since 2019/03/05
 */
@Getter
@Setter
public class SubscriptionWeEvent {
    private String subscriptionId;
    private WeEvent event;
}
