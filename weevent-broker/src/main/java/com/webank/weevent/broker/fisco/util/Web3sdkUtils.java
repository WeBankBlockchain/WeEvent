package com.webank.weevent.broker.fisco.util;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.webank.weevent.broker.config.FiscoConfig;
import com.webank.weevent.broker.fisco.web3sdk.Web3SDK2Wrapper;
import com.webank.weevent.broker.fisco.web3sdk.Web3SDKWrapper;
import com.webank.weevent.sdk.WeEvent;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Data
class TopicControlAddress {
    String address;
    Boolean isNew;

    TopicControlAddress(String address, Boolean isNew) {
        this.address = address;
        this.isNew = isNew;
    }
}

/**
 * Utils 4 web3sdk.
 *
 * @author matthewliu
 * @since 2019/02/12
 */
@Slf4j
public class Web3sdkUtils {
    /**
     * tool to deploy contract "TopicController", and save address back to block chain.
     * Usage:
     * java -Xbootclasspath/a:./config -cp weevent-broker-2.0.0.jar -Dloader.main=com.webank.weevent.broker.fisco.util.Web3sdkUtils org.springframework.boot.loader.PropertiesLauncher
     */
    public static void main(String[] args) {
        try {
            FiscoConfig fiscoConfig = new FiscoConfig();
            fiscoConfig.load();
            ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
            taskExecutor.initialize();

            if (fiscoConfig.getVersion().startsWith("2.")) {    // 2.0x
                org.fisco.bcos.web3j.crypto.Credentials credentials = Web3SDK2Wrapper.getCredentials(fiscoConfig);

                Map<Long, org.fisco.bcos.web3j.protocol.Web3j> groups = new HashMap<>();
                // 1 is always exist
                Long defaultGroup = Long.valueOf(WeEvent.DEFAULT_GROUP_ID);
                org.fisco.bcos.web3j.protocol.Web3j defaultWeb3j = Web3SDK2Wrapper.initWeb3j(defaultGroup, fiscoConfig, taskExecutor);
                groups.put(defaultGroup, defaultWeb3j);

                List<String> groupIds = Web3SDK2Wrapper.listGroupId(defaultWeb3j);
                groupIds.remove(WeEvent.DEFAULT_GROUP_ID);
                for (String groupId : groupIds) {
                    Long gid = Long.valueOf(groupId);
                    org.fisco.bcos.web3j.protocol.Web3j web3j = Web3SDK2Wrapper.initWeb3j(gid, fiscoConfig, taskExecutor);
                    groups.put(gid, web3j);
                }
                log.info("all group in nodes: {}", groups.keySet());

                // deploy topic control contract for every group
                Map<Long, TopicControlAddress> CRUDAddress = new HashMap<>();
                for (Map.Entry<Long, org.fisco.bcos.web3j.protocol.Web3j> e : groups.entrySet()) {
                    org.fisco.bcos.web3j.protocol.Web3j web3j = e.getValue();
                    // check exist first
                    String original = Web3SDK2Wrapper.getAddress(web3j, credentials);
                    if (!StringUtils.isBlank(original)) {
                        log.info("topic control address already exist, group: {} address: {}", e.getKey(), original);

                        CRUDAddress.put(e.getKey(), new TopicControlAddress(original, false));
                        continue;
                    }

                    // deploy topic control
                    String address = Web3SDK2Wrapper.deployTopicControl(web3j, credentials);
                    log.info("deploy topic control success, group: {} address: {}", e.getKey(), address);

                    // save topic control address into CRUD
                    boolean result = Web3SDK2Wrapper.addAddress(web3j, credentials, address);
                    log.info("save topic control address into CRUD, group: {} result: {}", e.getKey(), result);
                    if (result) {
                        CRUDAddress.put(e.getKey(), new TopicControlAddress(address, true));
                    }
                }

                System.out.println(nowTime() + " topic control address in every group:");
                for (Map.Entry<Long, TopicControlAddress> e : CRUDAddress.entrySet()) {
                    if (e.getValue().getIsNew()) {
                        System.out.println(e.getKey() + "\t" + e.getValue().getAddress() + "\tnew");
                    } else {
                        System.out.println(e.getKey() + "\t" + e.getValue().getAddress());
                    }
                }
            } else if (fiscoConfig.getVersion().startsWith("1.3")) {    // 1.x
                org.bcos.web3j.crypto.Credentials credentials = Web3SDKWrapper.getCredentials(fiscoConfig);
                org.bcos.web3j.protocol.Web3j web3j = Web3SDKWrapper.initWeb3j(fiscoConfig, taskExecutor);

                // check exist first
                String original = Web3SDKWrapper.getAddress(web3j, credentials, fiscoConfig.getProxyAddress());
                if (!StringUtils.isBlank(original)) {
                    log.info("topic control address already exist, address: {}", original);
                    System.out.println(nowTime() + " topic control address: " + original);
                    System.exit(1);
                }

                // deploy topic control
                String address = Web3SDKWrapper.deployTopicControl(web3j, credentials);
                log.info("deploy topic control address success, address: {}", address);

                // save topic control address into CNS
                boolean result = Web3SDKWrapper.addAddress(web3j, credentials, fiscoConfig.getProxyAddress(), address);
                log.info("save topic control address into CNS, result: {}", result);
                if (result) {
                    System.out.println(nowTime() + " topic control address: " + address + "\tnew");
                }
            } else {
                log.error("unknown FISCO-BCOS version: {}", fiscoConfig.getVersion());
                System.exit(1);
            }
        } catch (Exception e) {
            log.error("deploy topic control contract failed", e);
            System.exit(1);
        }

        // web3sdk can't exit gracefully
        System.exit(0);
    }

    private static String nowTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(new Date());
    }
}
