package com.webank.weevent.governance.junit;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.webank.weevent.governance.JUnitTestBase;
import com.webank.weevent.governance.utils.DAGDetectUtil;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class DAGDetectTest extends JUnitTestBase {


    @Autowired
    private DAGDetectUtil dagDetectUtil;


    @Before
    public void before() {
        log.info("=============================={}.{}==============================",
                this.getClass().getSimpleName(),
                this.testName.getMethodName());
    }

    @Test
    public void testNormal001() {
        Map<String, Set<String>> map = new HashMap<>();
        map.put("A", new HashSet<>(Arrays.asList("B", "C")));
        map.put("B", new HashSet<>(Arrays.asList("C", "E", "H", "I", "J")));
        boolean b = dagDetectUtil.checkLoop(map);
        Assert.assertTrue(b);
    }

    @Test
    public void testNormal002() {
        Map<String, Set<String>> map = new HashMap<>();
        map.put("A", new HashSet<>(Arrays.asList("B", "CEE", "DFF", "EAA", "FC")));
        map.put("B", new HashSet<>(Arrays.asList("C", "E", "H")));
        map.put("C", new HashSet<>(Arrays.asList("D", "E", "F", "H", "I")));
        boolean b = dagDetectUtil.checkLoop(map);
        Assert.assertTrue(b);
    }

    @Test
    public void testNormal003() {
        Map<String, Set<String>> map = new HashMap<>();
        map.put("A", new HashSet<>(Arrays.asList("B", "C", "D", "E", "F")));
        map.put("B", new HashSet<>(Arrays.asList("C", "E", "H", "I", "J")));
        map.put("C", new HashSet<>(Arrays.asList("D", "E", "F", "H", "I")));
        map.put("D", new HashSet<>(Arrays.asList("E", "AA", "X", "W", "Y")));
        boolean b = dagDetectUtil.checkLoop(map);
        Assert.assertTrue(b);
    }

    @Test
    public void testNormal004() {
        Map<String, Set<String>> map = new HashMap<>();
        map.put("A", new HashSet<>(Arrays.asList("B", "C", "D", "E", "F")));
        map.put("B", new HashSet<>(Arrays.asList("C", "E", "H", "I", "J")));
        map.put("C", new HashSet<>(Arrays.asList("D", "E", "F", "H", "I")));
        map.put("D", new HashSet<>(Arrays.asList("E", "AA", "X", "W", "Y")));
        boolean b = dagDetectUtil.checkLoop(map);
        Assert.assertTrue(b);
    }

    @Test
    public void testException001() {
        Map<String, Set<String>> map = new HashMap<>();
        map.put("A", new HashSet<>(Arrays.asList("B", "CEE", "DFF", "EAA", "FC")));
        map.put("B", new HashSet<>(Arrays.asList("A", "E", "H", "I", "DJ")));
        boolean b = dagDetectUtil.checkLoop(map);
        Assert.assertFalse(b);
    }

    @Test
    public void testException002() {
        Map<String, Set<String>> map = new HashMap<>();
        map.put("A", new HashSet<>(Arrays.asList("B", "CEE", "DFF", "EAA", "FC")));
        map.put("B", new HashSet<>(Arrays.asList("C", "E", "H", "I", "DJ")));
        map.put("C", new HashSet<>(Arrays.asList("D", "E", "B", "H", "I")));
        boolean b = dagDetectUtil.checkLoop(map);
        Assert.assertFalse(b);
    }

    @Test
    public void testException003() {
        Map<String, Set<String>> map = new HashMap<>();
        map.put("A", new HashSet<>(Arrays.asList("B", "CEE", "DFF", "EAA", "FC")));
        map.put("B", new HashSet<>(Arrays.asList("C", "E", "H", "I", "DJ")));
        map.put("C", new HashSet<>(Arrays.asList("D", "E", "F", "H", "I")));
        map.put("D", new HashSet<>(Arrays.asList("E", "A", "C", "W", "Y")));
        boolean b = dagDetectUtil.checkLoop(map);
        Assert.assertFalse(b);
    }

    @Test
    public void testException004() {
        Map<String, Set<String>> map = new HashMap<>();
        map.put("A", new HashSet<>(Arrays.asList("B", "CEE", "DFF", "EAA", "FC")));
        map.put("B", new HashSet<>(Arrays.asList("C", "E", "H", "I", "DJ")));
        map.put("C", new HashSet<>(Arrays.asList("D", "E", "F", "H", "I")));
        map.put("D", new HashSet<>(Arrays.asList("E", "Abc", "C", "W", "Y")));
        boolean b = dagDetectUtil.checkLoop(map);
        Assert.assertFalse(b);
    }


}
