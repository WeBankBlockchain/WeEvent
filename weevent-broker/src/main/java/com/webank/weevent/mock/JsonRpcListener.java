package com.webank.weevent.mock;


import com.webank.weevent.protocol.jsonrpc.IBrokerRpcCallback;
import com.webank.weevent.sdk.WeEvent;

import com.googlecode.jsonrpc4j.JsonRpcParam;
import com.googlecode.jsonrpc4j.spring.AutoJsonRpcServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Mock interface onEvent for JsonRPC subscribe.
 *
 * @author matthewliu
 * @since 2019/02/25
 */
@Slf4j
@AutoJsonRpcServiceImpl
@Component
public class JsonRpcListener implements IBrokerRpcCallback {
    @Override
    public void onEvent(@JsonRpcParam(value = "subscriptionId") String subscriptionId,
                        @JsonRpcParam(value = "event") WeEvent event) {
        log.info("mock json rpc onEvent, subscriptionId: {} event: {}", subscriptionId, event);
    }
}
