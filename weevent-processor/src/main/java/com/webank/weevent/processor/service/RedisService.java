package com.webank.weevent.processor.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.webank.weevent.processor.model.CEPRule;
import com.webank.weevent.processor.utils.ObjectTranscoder;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Slf4j
@Data
public class RedisService {

    private JedisPool jedisPool;

    public void writeRulesToRedis(String id, CEPRule rule) {
        try (Jedis jedis = getJedis()) {
            jedis.setnx(id.getBytes(), ObjectTranscoder.getInstance().serialize(rule));
        }
    }


    public void deleteRulesToRedis(String id) {
        try (Jedis jedis = getJedis()) {
            jedis.del(id.getBytes());
        }
    }

    public CEPRule readRulesFromRedis(String id) {
        try (Jedis jedis = getJedis()) {
            byte[] in = jedis.get(id.getBytes());
            CEPRule rule = (CEPRule) (ObjectTranscoder.getInstance().deserialize(in));
            log.info("id :{} rule name: {}", rule.getId(), rule.getRuleName());
            return rule;
        }
    }


    public Map<String, CEPRule> readAllRulesFromRedis(List<String> keys) {
        try (Jedis jedis = getJedis()) {
            Map<String, CEPRule> ruleMap = new ConcurrentHashMap<>();

            for (int i = 0; i < keys.size(); ++i) {
                log.info("keys:{}",keys.get(i));
                byte[] in = jedis.get(keys.get(i).getBytes());
                CEPRule rule = (CEPRule) (ObjectTranscoder.getInstance().deserialize(in));
                ruleMap.put(keys.get(i), rule);
            }
            return ruleMap;
        }
    }

    public boolean isRuleExistInRedis(String id) {
        try (Jedis jedis = getJedis()) {
            return jedis.exists(id);
        }
    }

    private Jedis getJedis() {
        return this.jedisPool.getResource();
    }

}