package com.webank.weevent.protocol.mqttbroker.store;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cn.hutool.core.util.StrUtil;
import org.springframework.stereotype.Service;

/**
 *@ClassName IRetainMessageStoreImpl
 *@Description TODO
 *@Author websterchen
 *@Date 2019/5/22 16:50
 *@Version 1.0
 **/
@Service
public class IRetainMessageStoreImpl implements IRetainMessageStore {

    private Map<String, RetainMessageStore> retainMessageCache = new ConcurrentHashMap();

    @Override
    public void put(String topic, RetainMessageStore retainMessageStore) {
        retainMessageCache.put(topic, retainMessageStore);
    }

    @Override
    public RetainMessageStore get(String topic) {
        return retainMessageCache.get(topic);
    }

    @Override
    public void remove(String topic) {
        retainMessageCache.remove(topic);
    }

    @Override
    public boolean containsKey(String topic) {
        return retainMessageCache.containsKey(topic);
    }

    @Override
    public List<RetainMessageStore> search(String topicFilter) {
        List<RetainMessageStore> retainMessageStores = new ArrayList<RetainMessageStore>();
        if (!StrUtil.contains(topicFilter, '#') && !StrUtil.contains(topicFilter, '+')) {
            if (retainMessageCache.containsKey(topicFilter)) {
                retainMessageStores.add(retainMessageCache.get(topicFilter));
            }
        } else {

        }
        return retainMessageStores;
    }
}
