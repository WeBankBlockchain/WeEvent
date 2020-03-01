package com.webank.weevent.core.config;


import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;

/**
 * load config without spring context.
 * support get file name from @PropertySource and system properties
 * Note: all properties can not be plain type, Must have constructor.
 *
 * @author matthewliu
 * @since 2019/10/22
 */
@Slf4j
public class SmartLoadConfig {
    /**
     * @param configObj object of config class
     * @param configFile config file
     * @param propertiesKey config file's key in system properties. "" if not care.
     * @return true if success
     */
    public boolean load(Object configObj, String configFile, String propertiesKey) {
        Class<?> clz = configObj.getClass();
        String file = configFile;
        if (StringUtils.isBlank(configFile)) {
            // custom properties from system properties, like junit test custom setting
            if (!StringUtils.isBlank(propertiesKey) && System.getProperties().containsKey(propertiesKey)) {
                log.info("get source file from System.getProperties[{}]", propertiesKey);

                file = System.getProperty(propertiesKey);
            } else {
                if (!clz.isAnnotationPresent(PropertySource.class)) {
                    log.error("use @PropertySource to set default configuration file name");
                    return false;
                }

                log.info("get source file from @PropertySource");

                PropertySource propertySource = clz.getAnnotation(PropertySource.class);
                String[] files = propertySource.value();
                if (!files[0].startsWith("classpath:")) {
                    log.error("configuration file must be in classpath");
                    return false;
                }
                file = files[0];
            }
        }
        log.info("load properties from file: {}", file);

        // be careful the path
        try (InputStream inputStream = clz.getClassLoader().getResourceAsStream(file.replace("classpath:", ""))) {
            Properties properties = new Properties();
            properties.load(inputStream);

            Field[] fields = clz.getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(Value.class)) {
                    Value value = field.getAnnotation(Value.class);

                    //String.split can not support this regex
                    Pattern pattern = Pattern.compile("\\$\\{(\\S+):(\\S*)}");
                    Matcher matcher = pattern.matcher(value.value());
                    String k = "";
                    String v = "";
                    if (matcher.find()) {
                        if (matcher.groupCount() >= 1) {
                            k = matcher.group(1);
                        }
                        if (matcher.groupCount() >= 2) {
                            v = matcher.group(2);
                        }
                    }

                    if (properties.containsKey(k)) {
                        v = properties.getProperty(k);
                    }
                    field.setAccessible(true);
                    Object obj = field.getType().getConstructor(String.class).newInstance(v);
                    field.set(configObj, obj);
                }
            }
        } catch (Exception e) {
            log.error("load properties failed", e);
            return false;
        }

        log.info("read from {}, {}", file, configObj);
        return true;
    }
}
