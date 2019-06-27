package com.webank.weevent.governance.utils;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

/**
 * JsonUtils
 */
@Slf4j
public class JsonUtils {

    // jackson class
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * convert class to json string。
     * <p>
     * Title: pojoToJson
     * </p>
     * <p>
     * Description:
     * </p>
     * 
     * @param data
     * @return
     */
    public static String objectToJson(Object data) {
        try {
            String string = MAPPER.writeValueAsString(data);
            return string;
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    /**
     * convert json to class
     * 
     * @param jsonData
     *            json
     * @param clazz
     * @return
     */
    public static <T> T jsonToPojo(String jsonData, Class<T> beanType) {
        try {
            T t = MAPPER.readValue(jsonData, beanType);
            return t;
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }

    /**
     * covert json to list
     * <p>
     * Title: jsonToList
     * </p>
     * <p>
     * Description:
     * </p>
     * 
     * @param jsonData
     * @param beanType
     * @return
     */
    public static <T> List<T> jsonToList(String jsonData, Class<T> beanType) {
        JavaType javaType = MAPPER.getTypeFactory().constructParametricType(List.class, beanType);
        try {
            List<T> list = MAPPER.readValue(jsonData, javaType);
            return list;
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return null;
    }

}
