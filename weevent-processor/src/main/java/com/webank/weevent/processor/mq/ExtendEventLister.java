package com.webank.weevent.processor.mq;

import java.util.Map;

import com.webank.weevent.processor.model.CEPRule;
import com.webank.weevent.processor.utils.CommonUtil;
import com.webank.weevent.sdk.IWeEventClient;
import com.webank.weevent.sdk.IWeEventClient.EventListener;
import com.webank.weevent.sdk.WeEvent;

import com.alibaba.fastjson.JSONException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExtendEventLister implements EventListener {
    private IWeEventClient client;
    private Map<String, CEPRule> ruleMap;


    ExtendEventLister(CEPRule rule, IWeEventClient client, Map<String, CEPRule> ruleMap) {
        this.client = client;
        this.ruleMap = ruleMap;

    }

    @Override
    public void onEvent(WeEvent event) {
        try {
            String content = new String(event.getContent());
            log.info("on event:{},content:{}", event.toString(), content);

            if (CommonUtil.checkValidJson(content)) {
                CEPRuleMQ.handleOnEvent(client, event, ruleMap);
            } else {
                CEPRuleMQ.handleOnEventOtherPattern(client, event, ruleMap);
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
