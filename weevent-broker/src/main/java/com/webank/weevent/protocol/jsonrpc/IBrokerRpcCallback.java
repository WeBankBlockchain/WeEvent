package com.webank.weevent.protocol.jsonrpc;


import com.webank.weevent.sdk.WeEvent;

import com.googlecode.jsonrpc4j.JsonRpcParam;
import com.googlecode.jsonrpc4j.JsonRpcService;

/**
 * Interface for subscribe callback.
 *
 * @author matthewliu
 * @since 2018/11/21
 */
@JsonRpcService("/mock/jsonrpc")
public interface IBrokerRpcCallback {
    void onEvent(@JsonRpcParam(value = "subscriptionId") String subscriptionId,
                 @JsonRpcParam(value = "events") WeEvent event) throws Exception;
}
