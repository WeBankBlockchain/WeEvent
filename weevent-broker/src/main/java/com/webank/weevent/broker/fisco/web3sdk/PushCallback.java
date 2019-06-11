package com.webank.weevent.broker.fisco.web3sdk;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.channel.client.ChannelPushCallback;
import org.fisco.bcos.channel.dto.ChannelPush;
import org.fisco.bcos.channel.dto.ChannelResponse;

/**
 * the AMOP channel push message to the client.
 */
@Slf4j
public class PushCallback extends ChannelPushCallback {

    @Override
    public void onPush(ChannelPush push) {
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        log.info("{} server:push the content {}", df.format(LocalDateTime.now()), push.getContent());
        log.info("PushCallback");
        ChannelResponse response = new ChannelResponse();
        response.setContent("receive request seq:" + String.valueOf(push.getMessageID()));
        response.setErrorCode(0);

        push.sendResponse(response);
        
    }
}

