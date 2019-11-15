package com.webank.weevent.governance.junit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.webank.weevent.governance.utils.DAGDetectUtil;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;

@Slf4j
public class DAGDetectTest {


    @Test
    public void testNormal001() {
        Map<String, Set<String>> map = new HashMap<>();
        map.put("A", new HashSet<>(Arrays.asList("B", "C", "D", "E", "F")));
        map.put("B", new HashSet<>(Arrays.asList("C", "E", "H", "I", "J")));
        map.put("C", new HashSet<>(Arrays.asList("D", "E", "F", "H", "I")));
        map.put("D", new HashSet<>(Arrays.asList("E", "AA", "X", "W", "Y")));
        Set<String> topicSet = map.keySet();
        boolean b = DAGDetectUtil.checkLoop(map, topicSet);
        Assert.assertTrue(b);
    }

    @Test
    public void testNormal002() {
        Map<String, Set<String>> map = new HashMap<>();
        map.put("A", new HashSet<>(Arrays.asList("B", "CEE", "DFF", "EAA", "FC")));
        map.put("B", new HashSet<>(Arrays.asList("C", "E", "H", "I", "DJ")));
        map.put("C", new HashSet<>(Arrays.asList("D", "E", "F", "H", "I")));
        Set<String> topicSet = map.keySet();
        boolean b = DAGDetectUtil.checkLoop(map, topicSet);
        Assert.assertTrue(b);
    }

    @Test
    public void testException001() {
        Map<String, Set<String>> map = new HashMap<>();
        map.put("A", new HashSet<>(Arrays.asList("B", "CEE", "DFF", "EAA", "FC")));
        map.put("B", new HashSet<>(Arrays.asList("C", "E", "H", "I", "DJ")));
        map.put("C", new HashSet<>(Arrays.asList("D", "E", "F", "H", "I")));
        map.put("D", new HashSet<>(Arrays.asList("E", "A", "X", "W", "Y")));
        Set<String> topicSet = map.keySet();
        boolean b = DAGDetectUtil.checkLoop(map, topicSet);
        Assert.assertFalse(b);
    }

    @Test
    public void testException002() {
        Map<String, Set<String>> map = new HashMap<>();
        map.put("A", new HashSet<>(Arrays.asList("B", "CEE", "DFF", "EAA", "FC")));
        map.put("B", new HashSet<>(Arrays.asList("C", "E", "H", "I", "DJ")));
        map.put("C", new HashSet<>(Arrays.asList("D", "E", "F", "H", "I")));
        map.put("D", new HashSet<>(Arrays.asList("E", "Abc", "C", "W", "Y")));
        Set<String> topicSet = map.keySet();
        boolean b = DAGDetectUtil.checkLoop(map, topicSet);
        Assert.assertFalse(b);
    }


}
