package com.webank.weevent.broker.fabric.config;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;

@Slf4j
@Data
@PropertySource(value = "classpath:fabric.properties", encoding = "UTF-8")
public class FabricConfig {
    @Value("${chain.channel.name:mychannel}")
    private String channelName;

    @Value("${chain.organizations.name:Org1}")
    private String orgName;

    @Value("${chain.organizations.mspid:Org1MSP}")
    private String mspId;

    @Value("${chain.organizations.username:Admin}")
    private String orgUserName;

    @Value("${chain.organizations.user.keyfile:}")
    private String orgUserKeyFile;

    @Value("${chain.organizations.user.certfile:}")
    private String orgUserCertFile;

    @Value("${chain.peer.address:}")
    private String peerAddress;

    @Value("${chain.peer.tls.cafile:}")
    private String peerTlsCaFile;

    @Value("${chain.orderer.address:}")
    private String ordererAddress;

    @Value("${chain.orderer.tls.cafile:}")
    private String ordererTlsCaFile;

    @Value("${chaincode.topic.version:v1.0}")
    private String topicVerison;

    @Value("${chaincode.topic.name:Topic}")
    private String topicName;

    @Value("${chaincode.topic.sourceloc:}")
    private String topicSourceLoc;

    @Value("${chaincode.topic.path:}")
    private String topicPath;

    @Value("${chaincode.topic-controller.version:v1.0}")
    private String topicControllerVersion;

    @Value("${chaincode.topic-controller.name:TopicController}")
    private String topicControllerName;

    @Value("${chaincode.topic-controller.sourceloc:}")
    private String topicControllerSourceLoc;

    @Value("${chaincode.topic-controller.path:}")
    private String topicControllerPath;

    /**
     * load configuration without spring
     *
     * @return true if success, else false
     */
    public boolean load() {
        if (!FabricConfig.class.isAnnotationPresent(PropertySource.class)) {
            log.error("set configuration file name use @PropertySource");
            return false;
        }

        PropertySource propertySource = FabricConfig.class.getAnnotation(PropertySource.class);
        String[] files = propertySource.value();
        if (!files[0].startsWith("classpath:")) {
            log.error("configuration file must be in classpath");
            return false;
        }
        log.info("load properties from file: {}", files[0]);

        // be careful the path
        String file = "/" + files[0].replace("classpath:", "");
        try (InputStream inputStream = FabricConfig.class.getResourceAsStream(file)) {
            Properties properties = new Properties();
            properties.load(inputStream);

            Field[] fields = FabricConfig.class.getDeclaredFields();
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
                    field.set(this, obj);
                }
            }
        } catch (Exception e) {
            log.error("load properties failed", e);
            return false;
        }

        log.info("read from fisco.properties: {}", this);
        return true;
    }
}
