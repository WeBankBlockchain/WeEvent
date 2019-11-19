package com.webank.weevent.governance.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.springframework.stereotype.Component;

@SuppressWarnings("unchecked")
@Component
public class DAGDetectUtil {
    private  Stack<String> stack = new Stack<>();

    public boolean checkLoop(Map<String, Set<String>> map) {
        Set<String> topicSet = map.keySet();
        Map start = createNode("start");
        Map<String, Map> nodeMap = new HashMap<>();
        topicSet.forEach(it -> {
            Map node = createNode(it);
            ((List) start.get("child")).add(node);
            nodeMap.put(it, node);
        });
        map.forEach((k, v) -> {
            v.forEach(it -> {
                if (nodeMap.containsKey(it)) {
                    ((List) nodeMap.get(k).get("child")).add(nodeMap.get(it));
                }
            });
        });
        return checkChild(start);
    }

    private Map createNode(String name) {
        HashMap node = new HashMap();
        node.put("topic", name);
        node.put("child", new ArrayList());
        return node;
    }


    private boolean checkChild(Map cursor) {
        if (stack.contains(cursor.get("topic"))) {
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
