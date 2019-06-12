package com.webank.weevent.broker.fisco.web3sdk;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.webank.weevent.broker.fisco.FiscoBcosBroker4Consumer;

import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.channel.client.ChannelPushCallback;
import org.fisco.bcos.channel.dto.ChannelPush;
import org.fisco.bcos.channel.dto.ChannelResponse;

import static java.lang.Boolean.TRUE;

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
//        ChannelResponse response = new ChannelResponse();
//        response.setContent("receive request seq:" + String.valueOf(push.getMessageID()));
//        response.setErrorCode(0);
//
//        push.sendResponse(response);

        // call the notify
        String content = push.getContent();
        if (content.contains(",")) {
            // for fisco bcos 2.0.* version
            String[] params = content.split(",");
            FiscoBcosBroker4Consumer.onNotify(Long.valueOf(params[0]), Long.valueOf(params[1]));
        } else {
            // for fisco bcos 1.3.*
            FiscoBcosBroker4Consumer.onNotify(Long.valueOf(content));
        }

    }
}

