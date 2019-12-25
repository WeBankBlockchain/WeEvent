package com.webank.weevent.governance.utils;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.webank.weevent.governance.entity.RuleEngineConditionEntity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapLikeType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;


@Slf4j
public class JsonUtil {


    private static ObjectMapper objectMapper = new ObjectMapper();

    static {
        // Include.NON_NULL Property is NULL and not serialized
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        //Do not convert inconsistent fields
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static <T> String toJSONString(T data) throws IOException {
        Assert.notNull(data, "data is null");
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            log.error("conversion of Json failed", e);
            throw new IOException("conversion of Json failed", e);
        }
    }

    public static <T> T parseObject(String data, Class<T> tClass) throws IOException {
        Assert.hasText(data, "data without text");
        try {
            return objectMapper.readValue(data, tClass);
        } catch (JsonProcessingException e) {
            log.error("conversion of Json failed", e);
            throw new IOException("conversion of Json failed", e);
        }
    }


    public static <T, R> Map<T, R> parseObjectToMap(String data, Class tclass1, Class tclass2) throws IOException {
        Assert.hasText(data, "data without text");
        try {
            MapLikeType mapLikeType = objectMapper.getTypeFactory().constructMapLikeType(Map.class, tclass1, tclass2);
            return objectMapper.readValue(data, mapLikeType);
        } catch (Exception e) {
            log.error("conversion of Json failed", e);
            throw new IOException("conversion of Json failed", e);
        }
    }

    public static <T> List<T> json2List(String jsonString) throws IOException {
        try {
            if (jsonString == null) {
                return null;
            }
            return objectMapper.readValue(jsonString, new TypeReference<List<RuleEngineConditionEntity>>() {});
        } catch (JsonProcessingException e) {
            log.error("convert jsonString to List failed ", e);
            throw new IOException("conversion of List failed", e);
        }
    }

    public static void main(String[] args) throws IOException {
        Map<String, String> map = new HashMap<>();
        RuleEngineConditionEntity ruleEngineConditionEntity = new RuleEngineConditionEntity();
        RuleEngineConditionEntity ruleEngineConditionEntity1 = new RuleEngineConditionEntity();
        RuleEngineConditionEntity ruleEngineConditionEntity2 = new RuleEngineConditionEntity();
        ruleEngineConditionEntity.setColumnMark("1424");
        ruleEngineConditionEntity.setColumnName("test");
        List<RuleEngineConditionEntity> list = Arrays.asList(ruleEngineConditionEntity);


        ruleEngineConditionEntity1.setColumnMark("1424");
        ruleEngineConditionEntity1.setColumnName("test1");
        List<RuleEngineConditionEntity> list1 = Arrays.asList(ruleEngineConditionEntity1);
        ruleEngineConditionEntity.setChildren(list1);
        ruleEngineConditionEntity2.setColumnMark("1424");
        ruleEngineConditionEntity2.setColumnName("test2");
        List<RuleEngineConditionEntity> list2 = Arrays.asList(ruleEngineConditionEntity2);
        ruleEngineConditionEntity1.setChildren(list2);
        map.put("columnName", ruleEngineConditionEntity.getColumnName());
        map.put("children", JsonUtil.toJSONString(list1));
        System.out.println(map.toString());
        String mapString = JsonUtil.toJSONString(map);
        Map<String, String> map1 = JsonUtil.parseObjectToMap(mapString, String.class, String.class);
        System.out.println(map1);
        List<RuleEngineConditionEntity> children = JsonUtil.objectMapper.readValue(map1.get("children"), new TypeReference<List<RuleEngineConditionEntity>>() {
        });
        assert children != null;
        ruleEngineConditionEntity.setChildren(children);

        String jsonString = JsonUtil.toJSONString(ruleEngineConditionEntity);
        RuleEngineConditionEntity ruleEngineConditionEntity3 = JsonUtil.parseObject(jsonString, RuleEngineConditionEntity.class);
        System.out.println(ruleEngineConditionEntity3.toString());
    }
}
