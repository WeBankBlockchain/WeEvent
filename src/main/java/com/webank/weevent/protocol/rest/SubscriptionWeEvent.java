package com.webank.weevent.protocol.rest;


import java.io.Serializable;

import com.webank.weevent.sdk.WeEvent;

import lombok.Data;

/**
 * WeEvent with subscription ID.
 *
 * @author matthewliu
 * @since 2019/03/05
 */
@Data
public class SubscriptionWeEvent implements Serializable {
    private String subscriptionId;
    private WeEvent event;
}
