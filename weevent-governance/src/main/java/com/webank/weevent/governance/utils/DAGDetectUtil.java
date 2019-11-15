package com.webank.weevent.governance.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

@SuppressWarnings("unchecked")
public class DAGDetectUtil {

    public static boolean checkLoop(Map<String, Set<String>> map, Set<String> topicSet) {
        Map start = createNode("start");
        Map<String, Map> nodeMap = new HashMap<>();
        topicSet.forEach(it -> {
            Map node = createNode(it);
            ((List) start.get("child")).add(node);
            nodeMap.put(it, node);
        });
        map.forEach((k, v) -> {
            v.forEach(it -> {
                if (nodeMap.get(it) != null) {
                    ((List) nodeMap.get(k).get("child")).add(nodeMap.get(it));
                }
            });
        });
        return checkChild(start);
    }

    private static Map createNode(String name) {
        HashMap node = new HashMap();
        node.put("topic", name);
        node.put("child", new ArrayList());
        return node;
    }

    private static Stack<String> stack = new Stack<>();

    private static boolean checkChild(Map cursor) {
        if (stack.contains(cursor.get("topic"))) {
            stack = new Stack<>();
            return false;
        }
        stack.push((String) cursor.get("topic"));
        List childs = (List) cursor.get("child");
        if (childs != null) {
            for (Object child : childs) {
                if (!checkChild((Map) child)) {
                    return false;
                }
            }
        }
        stack.pop();
        return true;
    }


}
