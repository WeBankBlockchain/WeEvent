package com.webank.weevent.protocol.mqttbroker.store.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import com.webank.weevent.protocol.mqttbroker.store.IRetainMessageStore;
import com.webank.weevent.protocol.mqttbroker.store.dto.RetainMessageStore;

import cn.hutool.core.util.StrUtil;
import org.apache.ignite.IgniteCache;
import org.springframework.stereotype.Service;

/**
 * @author websterchen
 * @version v1.0
 * @since 2019/6/3
 */
@Service
public class IRetainMessageStoreImpl implements IRetainMessageStore {
    @Resource
    private IgniteCache<String, RetainMessageStore> retainMessageCache;

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
            retainMessageCache.forEach(entry -> {
                String topic = entry.getKey();
                if (StrUtil.split(topic, '/').size() >= StrUtil.split(topicFilter, '/').size()) {
                    List<String> splitTopics = StrUtil.split(topic, '/');
                    List<String> spliteTopicFilters = StrUtil.split(topicFilter, '/');
                    String newTopicFilter = "";
                    for (int i = 0; i < spliteTopicFilters.size(); i++) {
                        String value = spliteTopicFilters.get(i);
                        if (value.equals("+")) {
                            newTopicFilter = newTopicFilter + "+/";
                        } else if (value.equals("#")) {
                            newTopicFilter = newTopicFilter + "#/";
                            break;
                        } else {
                            newTopicFilter = newTopicFilter + splitTopics.get(i) + "/";
                        }
                    }
                    newTopicFilter = StrUtil.removeSuffix(newTopicFilter, "/");
                    if (topicFilter.equals(newTopicFilter)) {
                        RetainMessageStore retainMessageStore = entry.getValue();
                        retainMessageStores.add(retainMessageStore);
                    }
                }
            });
        }
        return retainMessageStores;
    }
}
