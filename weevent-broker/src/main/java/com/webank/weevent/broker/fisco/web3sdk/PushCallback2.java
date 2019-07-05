package com.webank.weevent.broker.fisco.web3sdk;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.webank.weevent.broker.fisco.FiscoBcosBroker4Consumer;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.bcos.channel.client.ChannelPushCallback;
import org.bcos.channel.dto.ChannelPush;

/**
 * the AMOP channel push message to the client.
 */
@Slf4j
public class PushCallback2 extends ChannelPushCallback {

    @Override
    public void onPush(ChannelPush push) {
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        log.info("{} server:push the content {}", df.format(LocalDateTime.now()), push.getContent());
        log.info("PushCallback");
        String content = push.getContent();
        JSONObject.parseObject(content).getString("groupId");
        FiscoBcosBroker4Consumer.onNotify(Long.valueOf(JSONObject.parseObject(content).getString("groupId")), Long.valueOf(JSONObject.parseObject(content).getString("blockNumber")));

    }
}

