package com.webank.weevent.processor.mq;

import com.alibaba.fastjson.JSONException;
import com.webank.weevent.processor.service.AnalysisWeEventIdService;
import com.webank.weevent.processor.utils.CommonUtil;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.IWeEventClient;
import com.webank.weevent.sdk.IWeEventClient.EventListener;
import com.webank.weevent.sdk.WeEvent;

public class MyEventLister implements IWeEventClient.EventListener {

    @Override
    public void onEvent(WeEvent event) {

    }

    @Override
    public void onException(Throwable e) {
    }

}
