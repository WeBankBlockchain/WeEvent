package com.webank.weevent.processor.service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;

import com.webank.weevent.processor.mapper.CEPRuleMapper;
import com.webank.weevent.processor.model.CEPRule;
import com.webank.weevent.sdk.WeEvent;
import com.webank.weevent.sdk.WeEventClient;
import com.webank.weevent.sdk.jms.WeEventConnectionFactory;
import com.webank.weevent.sdk.jms.WeEventTopic;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class InitRule {
    private class InitRuleThread implements Runnable {

        public void run() {
            try {
                for (int i = 0; i < dynamicRuleList.size(); i++) {
                    TopicConnectionFactory connectionFactory = new WeEventConnectionFactory(dynamicRuleList.get(i).getBrokerUrl());
                    TopicConnection connection = connectionFactory.createTopicConnection();
                    // start connection
                    connection.start();
                    // create session
                    TopicSession session = connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
                    System.out.println("ticket = " + dynamicRuleList.get(i).getBrokerUrl() + "" + dynamicRuleList.get(i).getId());

                    // create topic
                    Topic topic = session.createTopic(dynamicRuleList.get(i).getToDestination());
                    // optional, default is OFFSET_LAST
                    ((WeEventTopic) topic).setOffset(WeEvent.OFFSET_LAST);
                    ((WeEventTopic) topic).setGroupId("1");//if not set default 1

                    // create subscriber
                    TopicSubscriber subscriber = session.createSubscriber(topic);

                    // create listener
                    subscriber.setMessageListener(new MessageListener() {
                        public void onMessage(Message message) {
                            BytesMessage msg = (BytesMessage) message;
                            try {
                                byte[] data = new byte[(int) msg.getBodyLength()];
                                msg.readBytes(data);
                                System.out.println("received: " + new String(data, StandardCharsets.UTF_8));
                            } catch (JMSException e) {
                                log.info("JMSException{}",e.toString());
                            }
                        }
                    });
                }
            } catch (JMSException e) {
                log.info("JMSException{}",e.toString());
            }
        }

    }

    //private static final Logger LOGGER = LoggerFactory.getLogger(InitRule.class);
    private static HashMap<String, List<String>> mapSave = new HashMap<>();

    private static Map<String, List<CEPRule>> ruleMap = new HashMap<>();
    // private static HashMap<String,List<String>> mapSave = new HashMap<>();
    static List<CEPRule> dynamicRuleList;

    private static List<String> brokerUrlList;

    @Autowired
    private CEPRuleMapper cEPRuleMapper;

    @PostConstruct
    public void init() {
        initMap();
    }

    // 1. 内存中，订阅消息，能够收到消息
    public List<CEPRule> initMap() {
        // get all rule
        dynamicRuleList = cEPRuleMapper.getDynamicCEPRuleList();
        String key = Integer.toString(ruleMap.size());
        ruleMap.putIfAbsent(key, dynamicRuleList);
        if (ruleMap != null && ruleMap.size() > 0) {
            Iterator<Map.Entry<String, List<CEPRule>>> it = ruleMap.entrySet().iterator(); //利用迭代器循环输出
            List<String> field = new ArrayList<>();
            List<CEPRule> value = new ArrayList<>();
            while (it.hasNext()) {
                Map.Entry<String, List<CEPRule>> entry = it.next();
                System.out.println("key=" + entry.getKey() + "," + "value=" + entry.getValue());
                field.add(entry.getKey().toString());
                value = entry.getValue();
            }
            System.out.println("keyList=" + field + "," + "CEPRule valueList=" + value); //输出为List类型
        }
        subscriptionTopic();
        return dynamicRuleList;
    }

    public List<String> initbrokerListMap() {
        // get all broker url
        brokerUrlList = cEPRuleMapper.getBrokerUrlList();
        return brokerUrlList;
    }

    private void subscriptionTopic() {
        InitRuleThread initRule = new InitRuleThread();
        new Thread(initRule).run();
    }
}

