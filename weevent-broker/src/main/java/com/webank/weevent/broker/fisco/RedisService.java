package com.webank.weevent.broker.fisco;

import java.util.List;

import com.webank.weevent.sdk.WeEvent;

import lombok.Data;
import org.springframework.data.redis.core.RedisTemplate;

@Data
public class RedisService {

    private RedisTemplate<String, List<WeEvent>> redisTemplate;

    public void writeEventsToRedis(String blockNum, List<WeEvent> list) {
        redisTemplate.opsForValue().set(blockNum, list);
    }

    public List<WeEvent> readEventsFromRedis(String blockNum) {
        return redisTemplate.opsForValue().get(blockNum);
    }

    public boolean isEventsExistInRedis(String blocknum) {
        return redisTemplate.hasKey(blocknum);
    }
}
