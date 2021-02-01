package com.webank.weevent.core.config;

import java.io.InputStream;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.sdk.config.model.ConfigProperty;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

/**
 * FISCO-BCOS Config that support loaded by spring context and pure java
 *
 * @author matthewliu
 * @version 1.0
 * @since 2019/1/28
 */
@Slf4j
@Getter
@Setter
@ToString
@Component
@PropertySource(value = "classpath:fisco.yml", encoding = "UTF-8")
public class FiscoConfig {

    private ConfigProperty configProperty;

    private WeEventCoreConfig weEventCoreConfig;

    /**
     * load configuration without spring
     *
     * @param configFile config file, if empty load from default location
     * @return true if success, else false
     */
    public boolean load(String configFile) {
        Class<?> clz = this.getClass();

        String file = configFile;
        if (StringUtils.isBlank(configFile)) {
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
        log.info("load properties from file: {}", file);

        Representer representer = new Representer();
        representer.getPropertyUtils().setSkipMissingProperties(true);
        Yaml yaml = new Yaml( representer);

        try (InputStream inputStream = clz.getClassLoader().getResourceAsStream(file.replace("classpath:", ""))) {
            this.configProperty = yaml.loadAs(inputStream, ConfigProperty.class);
        } catch (Exception e) {
            log.error("parse configFile error:{} {}", configFile, e);
            return false;
        }

        try (InputStream inputStream = clz.getClassLoader().getResourceAsStream(file.replace("classpath:", ""))) {
            this.weEventCoreConfig = yaml.loadAs(inputStream, WeEventCoreConfig.class);
        } catch (Exception e) {
            log.error("parse configFile error:{} {}", configFile, e);
            return false;
        }

        log.info("FiscoConfig:{} {}", this.getConfigProperty(), this.getWeEventCoreConfig().toString());
        return true;
    }
}
