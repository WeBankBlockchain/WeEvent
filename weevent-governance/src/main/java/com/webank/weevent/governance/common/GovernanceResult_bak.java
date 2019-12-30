package com.webank.weevent.governance.common;

import java.util.List;

import com.webank.weevent.governance.code.ErrorCode;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class GovernanceResult_bak {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    // response status
    private Integer status;

    // response msg
    private String msg;

    // response data
    private Object data;

    // total number;
    private Integer totalCount;


    public static GovernanceResult_bak build(Integer status, String msg, Object data) {
        return new GovernanceResult_bak(status, msg, data);
    }

    public static GovernanceResult_bak ok(Object data) {
        return new GovernanceResult_bak(data);
    }

    public static GovernanceResult_bak ok() {
        return new GovernanceResult_bak(200,"OK",null);
    }

    public GovernanceResult_bak() {

    }

    public static GovernanceResult_bak build(Integer status, String msg) {
        return new GovernanceResult_bak(status, msg, null);
    }

    public GovernanceResult_bak(Integer status, String msg, Object data) {
        this.status = status;
        this.msg = msg;
        this.data = data;
    }

    public GovernanceResult_bak(ErrorCode errorCode) {
        this.status = errorCode.getCode();
        this.msg = errorCode.getCodeDesc();
        this.data = null;
    }

    public GovernanceResult_bak(Object data) {
        this.status = 200;
        this.msg = "OK";
        this.data = data;
    }

    /**
     * @param jsonData jsondat
     * @param clazz object type
     * @return
     */
    public static GovernanceResult_bak formatToPojo(String jsonData, Class<?> clazz) {
        try {
            if (clazz == null) {
                return MAPPER.readValue(jsonData, GovernanceResult_bak.class);
            }
            JsonNode jsonNode = MAPPER.readTree(jsonData);
            JsonNode data = jsonNode.get("data");
            Object obj = null;
            if (clazz != null) {
                if (data.isObject()) {
                    obj = MAPPER.readValue(data.traverse(), clazz);
                } else if (data.isTextual()) {
                    obj = MAPPER.readValue(data.asText(), clazz);
                }
            }
            return build(jsonNode.get("status").intValue(), jsonNode.get("msg").asText(), obj);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * no object class covert
     *
     * @param json
     * @return
     */
    public static GovernanceResult_bak format(String json) {
        try {
            return MAPPER.readValue(json, GovernanceResult_bak.class);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }

    /**
     * Object is list covert
     *
     * @param jsonData jsondata
     * @param clazz
     * @return
     */
    public static GovernanceResult_bak formatToList(String jsonData, Class<?> clazz) {
        try {
            JsonNode jsonNode = MAPPER.readTree(jsonData);
            JsonNode data = jsonNode.get("data");
            Object obj = null;
            if (data.isArray() && data.size() > 0) {
                obj = MAPPER.readValue(data.traverse(),
                        MAPPER.getTypeFactory().constructCollectionType(List.class, clazz));
            }
            return build(jsonNode.get("status").intValue(), jsonNode.get("msg").asText(), obj);
        } catch (Exception e) {
            return null;
        }
    }
}
