package com.webank.weevent.client;


import lombok.Getter;
import lombok.Setter;

/**
 * plus information on block chain.
 *
 * @author matthewliu
 * @since 2020/03/16
 */
@Getter
@Setter
public class WeEventPlus {
    long timestamp;
    long height;
    String txHash;
    String sender;

    private WeEventPlus() {
    }

    public WeEventPlus(long timestamp, long height, String txHash, String sender) {
        this.timestamp = timestamp;
        this.height = height;
        this.txHash = txHash;
        this.sender = sender;
    }
}
