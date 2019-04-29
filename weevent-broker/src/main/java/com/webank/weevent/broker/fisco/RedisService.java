package com.webank.weevent.broker.fisco;

import java.util.List;

import com.webank.weevent.broker.fisco.util.SerializeUtils;
import com.webank.weevent.sdk.WeEvent;

import lombok.Data;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Data
public class RedisService {

    private JedisPool jedisPool;

    public void writeEventsToRedis(String blockNum, List<WeEvent> list) {
        try (Jedis jedis = getJedis()) {
            jedis.setnx(blockNum.getBytes(), SerializeUtils.serializeList(list));
        }
    }

    public List<WeEvent> readEventsFromRedis(String blockNum) {
        try (Jedis jedis = getJedis()) {
            byte[] in = jedis.get(blockNum.getBytes());
            return SerializeUtils.deserializeList(in);
        }
    }

    public boolean isEventsExistInRedis(String blocknum) {
        try (Jedis jedis = getJedis()) {
            return jedis.exists(blocknum);
        }
    }

    private Jedis getJedis() {
        return this.jedisPool.getResource();
    }

}
