package com.webank.weevent.broker.ha;


import java.io.Serializable;

import lombok.Data;

/**
 * Serializable object in zookeeper.
 *
 * @author matthewliu
 * @since 2019/03/13
 */
@Data
public class ZKSubscription implements Serializable {
    private String topic;
    private String subscriptionId;

    // for json rpc and restful
    // false is json rpc
    private Boolean restful;
    private String callbackUrl;
}
