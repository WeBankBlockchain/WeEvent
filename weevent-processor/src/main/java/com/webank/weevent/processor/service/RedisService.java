package com.webank.weevent.processor.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.webank.weevent.processor.model.CEPRule;
import com.webank.weevent.processor.utils.SerializeUtils;
import com.webank.weevent.processor.utils.Util;
import com.webank.weevent.sdk.WeEvent;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Slf4j
@Data
public class RedisService {

    private JedisPool jedisPool;

    public void writeRulesToRedis(String id, CEPRule rule) {
        try (Jedis jedis = getJedis()) {
            jedis.setnx(id.getBytes(), SerializeUtils.serialize(rule.toString().getBytes()));
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
            return SerializeUtils.deserialize(in);
        }
    }

    public Map<String, CEPRule> readAllRulesFromRedis() {
        try (Jedis jedis = getJedis()) {
            Set<byte[]> keySet = jedis.keys("*".getBytes());
            byte[][] keys = keySet.toArray(new byte[keySet.size()][]);

            byte[][] values = jedis.mget(keys).toArray(new byte[keySet.size()][]);

            Map<String, CEPRule> ruleMap = new ConcurrentHashMap<>();
            for (int i = 0; i < keySet.size(); ++i) {
                log.info(Util.byte2hex(keys[i]) + " --- " + Util.byte2hex(values[i]));
                ruleMap.put(Util.byte2hex(keys[i]), SerializeUtils.deserialize(values[i]));
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