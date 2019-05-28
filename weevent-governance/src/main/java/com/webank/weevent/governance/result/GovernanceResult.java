package com.webank.weevent.governance.result;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GovernanceResult {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    // response status
    private Integer status;

    // response msg
    private String msg;

    // response data
    private Object data;

    public static GovernanceResult build(Integer status, String msg, Object data) {
	return new GovernanceResult(status, msg, data);
    }

    public static GovernanceResult ok(Object data) {
	return new GovernanceResult(data);
    }

    public static GovernanceResult ok() {
	return new GovernanceResult(null);
    }

    public GovernanceResult() {

    }

    public static GovernanceResult build(Integer status, String msg) {
	return new GovernanceResult(status, msg, null);
    }

    public GovernanceResult(Integer status, String msg, Object data) {
	this.status = status;
	this.msg = msg;
	this.data = data;
    }

    public GovernanceResult(Object data) {
	this.status = 200;
	this.msg = "OK";
	this.data = data;
    }

    public Integer getStatus() {
	return status;
    }

    public void setStatus(Integer status) {
	this.status = status;
    }

    public String getMsg() {
	return msg;
    }

    public void setMsg(String msg) {
	this.msg = msg;
    }

    public Object getData() {
	return data;
    }

    public void setData(Object data) {
	this.data = data;
    }

    /**
     * 
     * @param jsonData
     *            jsondat
     * @param clazz
     *            object type
     * @return
     */
    public static GovernanceResult formatToPojo(String jsonData, Class<?> clazz) {
	try {
	    if (clazz == null) {
		return MAPPER.readValue(jsonData, GovernanceResult.class);
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
    public static GovernanceResult format(String json) {
	try {
	    return MAPPER.readValue(json, GovernanceResult.class);
	} catch (Exception e) {
	    e.printStackTrace();
	}
	return null;
    }

    /**
     * Object is list covert
     * 
     * @param jsonData
     *            jsondata
     * @param clazz
     * @return
     */
    public static GovernanceResult formatToList(String jsonData, Class<?> clazz) {
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
