package com.webank.weevent.processor;

import com.webank.weevent.processor.model.CEPRule;
import com.webank.weevent.processor.utils.ObjectTranscoder;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

@Slf4j
public class RedisServiceTest {
    @Test
    public void testRedisRuleCache() throws Exception {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        JedisPool jedisPool = new JedisPool(jedisPoolConfig,
                "127.0.0.1",
                6379,
                Protocol.DEFAULT_TIMEOUT,
                "foobared");
        jedisPool.getResource().ping();
        Jedis jedis = jedisPool.getResource();

        String bbb = "{\"brokerUrl\":\"http://127.0.0.1:8090/weevent\",\"conditionField\":\"temperate>35\",\"conditionType\":1,\"createdTime\":1570696435791,\"databaseUrl\":\"meifen\",\"errorCode\":\"1\",\"errorDestination\":\"test\",\"errorMessage\":\"test\",\"fromDestination\":\"com.webank.weevent.from\",\"id\":\"106\",\"payloadType\":0,\"ruleName\":\"air3\",\"selectField\":\"temperate\",\"status\":2,\"toDestination\":\"com.webank.weevent.test\",\"updatedTime\":1570696435791}";


        JSONObject obj = JSONObject.parseObject(bbb);

        CEPRule r = (CEPRule) JSONObject.toJavaObject(obj, CEPRule.class);
        log.info("before:{}", r.getRuleName());

        byte[] idb = r.getId().getBytes();
        jedis.setnx(idb, ObjectTranscoder.getInstance().serialize(r));
        byte[] in = jedis.get(idb);

        System.out.println("length:{}" + in.length);
        CEPRule r3 = (CEPRule) (ObjectTranscoder.getInstance().deserialize(in));
        log.info("after:{}", r3.getRuleName());
        log.info("after:{}", JSONObject.toJSONString(r3));
        Assert.assertEquals("air3", r3.getRuleName());
    }

    @Test
    public void testRedisRuleCacheString() throws Exception {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        JedisPool jedisPool = new JedisPool(jedisPoolConfig,
                "127.0.0.1",
                6379,
                Protocol.DEFAULT_TIMEOUT,
                "foobared");
        jedisPool.getResource().ping();
        Jedis jedis = jedisPool.getResource();

        String bbb = " {\n" +
                " \t\t\"id\":1,\n" +
                "        \"ruleName\": \"air3\",\n" +
                "        \"fromDestination\": \"from.com.webank.weevent\",\n" +
                "        \"brokerUrl\": \"http://127.0.0.1:8090/weevent\",\n" +
                "        \"payload\":\"{\\\"studentName\\\":\\\"lily\\\",\\\"studentAge\\\":12}\",\n" +
                "        \"payloadType\": 0,\n" +
                "        \"selectField\": null,\n" +
                "        \"conditionField\": \"temperate>35\",\n" +
                "        \"conditionType\": 1,\n" +
                "        \"toDestination\": \"to.com.webank.weevent\",\n" +
                "        \"databaseurl\": \"jdbc:mysql://127.0.0.1:3306/cep?user=root&password=password\",\n" +
                "        \"createdTime\": \"2019-08-23T18:09:16.000+0000\",\n" +
                "        \"status\": 1,\n" +
                "        \"errorDestination\": null,\n" +
                "        \"errorCode\": null,\n" +
                "        \"errorMessage\": null,\n" +
                "        \"updatedTime\": \"2019-08-23T18:09:16.000+0000\"\n" +
                "    }";

        JSONObject obj = JSONObject.parseObject(bbb);

        CEPRule r = (CEPRule) JSONObject.toJavaObject(obj, CEPRule.class);
        log.info("before:{}", r.getRuleName());

        byte[] idb = r.getId().getBytes();
        jedis.setnx(idb, ObjectTranscoder.getInstance().serialize(r));
        byte[] in = jedis.get(idb);

        System.out.println("length:{}" + in.length);
        CEPRule r3 = (CEPRule) (ObjectTranscoder.getInstance().deserialize(in));
        log.info("after:{}", r3.getRuleName());
        log.info("after:{}", JSONObject.toJSONString(r3));
        Assert.assertEquals("air3", r3.getRuleName());
    }


    @Test
    public void testRedisRuleCacheSpecialWrong() throws Exception {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        JedisPool jedisPool = new JedisPool(jedisPoolConfig,
                "127.0.0.1",
                6379,
                Protocol.DEFAULT_TIMEOUT,
                "foobared");
        jedisPool.getResource().ping();
        Jedis jedis = jedisPool.getResource();

        String bbb = "{\"id\":\"6\",\"ruleName\":\"air3\"}";

        JSONObject obj = JSONObject.parseObject(bbb);

        CEPRule r = (CEPRule) JSONObject.toJavaObject(obj, CEPRule.class);
        log.info("before:{}", r.getRuleName());

        byte[] idb = r.getId().getBytes();
        jedis.setnx(idb, ObjectTranscoder.getInstance().serialize(r));
        byte[] in = jedis.get(idb);

        System.out.println("length:{}" + in.length);
        CEPRule r3 = (CEPRule) (ObjectTranscoder.getInstance().deserialize(in));
        log.info("after:{}", r3.getRuleName());
        log.info("after:{}", JSONObject.toJSONString(r3));
        Assert.assertEquals("air3", r3.getRuleName());
    }
}
