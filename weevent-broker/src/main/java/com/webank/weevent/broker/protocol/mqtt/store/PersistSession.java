package com.webank.weevent.broker.protocol.mqtt.store;


import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * Session persist in zookeeper.
 *
 * @author matthewliu
 * @since 2020/04/21
 */
@Getter
@Setter
public class PersistSession {
    private String clientId;
    private List<SubscribeData> subscribeDataList = new ArrayList<>();

    public PersistSession(String clientId) {
        this.clientId = clientId;
    }

    // jackson need
    private PersistSession() {
    }
}
