package com.webank.weevent.processor.mq;

import com.webank.weevent.processor.utils.CommonUtil;
import com.webank.weevent.sdk.IWeEventClient;
import com.webank.weevent.sdk.IWeEventClient.EventListener;
import com.webank.weevent.sdk.WeEvent;

import com.alibaba.fastjson.JSONException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExtendEventLister implements EventListener {
    private IWeEventClient client;


    ExtendEventLister(IWeEventClient client) {
        this.client = client;
    }

    @Override
    public void onEvent(WeEvent event) {
        try {
            String content = new String(event.getContent());
            log.info("on event:{},content:{}", event.toString(), content);

            if (CommonUtil.checkValidJson(content)) {
                CEPRuleMQ.handleOnEvent(client, event);
            } else {
                CEPRuleMQ.handleOnEventOtherPattern(client, event);
            }
        } catch (JSONException e) {
            log.error(e.toString());
        }
    }

    @Override
    public void onException(Throwable e) {
        log.info("on event:{}", e.toString());
    }

}
