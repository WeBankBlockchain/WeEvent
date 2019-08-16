package com.webank.weevent.broker.fisco.util;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.webank.weevent.broker.config.FiscoConfig;
import com.webank.weevent.broker.fisco.web3sdk.Web3SDK2Wrapper;
import com.webank.weevent.broker.fisco.web3sdk.Web3SDKWrapper;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;


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
     * java -Xbootclasspath/a:./config -cp weevent-broker-2.0.0.jar -Dloader.main=com.webank.weevent.broker.fisco.util.Web3sdkUtils org.springframework.boot.loader.PropertiesLauncher [1]
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
                org.fisco.bcos.web3j.protocol.Web3j web3j = Web3SDK2Wrapper.initWeb3j(1L, fiscoConfig, taskExecutor);
                groups.put(1L, web3j);
                List<String> groupIds = Web3SDK2Wrapper.listGroupId(web3j);
                System.out.println("all group in nodes: {}" + groups.toString());
                groupIds.remove(1L);
                for (String groupId : groupIds) {
                    Long gid = Long.valueOf(groupId);
                    web3j = Web3SDK2Wrapper.initWeb3j(gid, fiscoConfig, taskExecutor);
                    groups.put(gid, web3j);
                }

                // deploy contract for every group
                for (Map.Entry<Long, org.fisco.bcos.web3j.protocol.Web3j> e : groups.entrySet()) {
                    // check exist first
                    String original = Web3SDK2Wrapper.getAddress(e.getValue(), credentials);
                    if (!StringUtils.isBlank(original)) {
                        System.out.println("contract[TopicController] already exist, group: " + e.getKey() + " address: " + original);
                        continue;
                    }

                    // deploy topic control
                    String address = Web3SDK2Wrapper.deployTopicControl(web3j, credentials);
                    System.out.println("deploy contract[TopicController] success, group: " + e.getKey() + " address: " + address);

                    // save topic control address into CRUD
                    boolean result = Web3SDK2Wrapper.addAddress(web3j, credentials, address);
                    System.out.println("save contract[TopicController] address into CRUD, group: " + e.getKey() + " result: {}" + result);
                }
            } else if (fiscoConfig.getVersion().startsWith("1.3")) {    // 1.x
                org.bcos.web3j.crypto.Credentials credentials = Web3SDKWrapper.getCredentials(fiscoConfig);
                org.bcos.web3j.protocol.Web3j web3j = Web3SDKWrapper.initWeb3j(fiscoConfig, taskExecutor);

                // check exist first
                String original = Web3SDKWrapper.getAddress(web3j, credentials, fiscoConfig.getProxyAddress());
                if (!StringUtils.isBlank(original)) {
                    System.out.println("contract[TopicController] already exist, address: " + original);
                    System.exit(1);
                }

                // deploy topic control
                String address = Web3SDKWrapper.deployTopicControl(web3j, credentials);
                System.out.println("deploy contract[TopicController] success, address: " + address);

                // save topic control address into CNS
                boolean result = Web3SDKWrapper.addAddress(web3j, credentials, fiscoConfig.getProxyAddress(), address);
                System.out.println("save contract[TopicController] address into CNS, result: " + result);
            } else {
                System.out.println("unknown FISCO-BCOS version: " + fiscoConfig.getVersion());
                System.exit(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        // web3sdk can't exit gracefully
        System.exit(0);
    }
}
