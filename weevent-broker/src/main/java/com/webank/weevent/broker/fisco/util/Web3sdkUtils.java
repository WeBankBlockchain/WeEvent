package com.webank.weevent.broker.fisco.util;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.webank.weevent.broker.config.FiscoConfig;
import com.webank.weevent.broker.fisco.constant.WeEventConstants;
import com.webank.weevent.broker.fisco.web3sdk.v1.Web3SDKWrapper;
import com.webank.weevent.broker.fisco.web3sdk.v2.SupportedVersion;
import com.webank.weevent.broker.fisco.web3sdk.v2.Web3SDK2Wrapper;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.WeEvent;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Data
class EchoAddress {
    private Long version;
    private String address;
    private Boolean isNew;

    EchoAddress(Long version, String address, Boolean isNew) {
        this.version = version;
        this.address = address;
        this.isNew = isNew;
    }

    @Override
    public String toString() {
        return String.format("version: %d\taddress: %s\tnew: %b", this.version, this.address, this.isNew);
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

            if (StringUtils.isBlank(fiscoConfig.getVersion())) {
                log.error("empty FISCO-BCOS version in fisco.properties");
                systemExit(1);
            }

            if (fiscoConfig.getVersion().startsWith(WeEventConstants.FISCO_BCOS_2_X_VERSION_PREFIX)) {    // 2.0x
                if (!deployV2Contract(fiscoConfig)) {
                    systemExit(1);
                }
            } else if (fiscoConfig.getVersion().startsWith(WeEventConstants.FISCO_BCOS_1_X_VERSION_PREFIX)) {    // 1.x
                if (!deployV1Contract(fiscoConfig)) {
                    systemExit(1);
                }
            } else {
                log.error("unknown FISCO-BCOS version: {}", fiscoConfig.getVersion());
                systemExit(1);
            }
        } catch (Exception e) {
            log.error("deploy topic control contract failed", e);
            systemExit(1);
        }

        // web3sdk can't exit gracefully
        systemExit(0);
    }

    private static boolean deployV2Contract(FiscoConfig fiscoConfig) throws BrokerException {
        org.fisco.bcos.web3j.crypto.Credentials credentials = Web3SDK2Wrapper.getCredentials(fiscoConfig);

        Map<Long, org.fisco.bcos.web3j.protocol.Web3j> groups = new HashMap<>();
        // 1 is always exist
        Long defaultGroup = Long.valueOf(WeEvent.DEFAULT_GROUP_ID);
        org.fisco.bcos.web3j.protocol.Web3j defaultWeb3j = Web3SDK2Wrapper.initWeb3j(defaultGroup, fiscoConfig);
        groups.put(defaultGroup, defaultWeb3j);

        List<String> groupIds = Web3SDK2Wrapper.listGroupId(defaultWeb3j);
        groupIds.remove(WeEvent.DEFAULT_GROUP_ID);
        for (String groupId : groupIds) {
            Long gid = Long.valueOf(groupId);
            org.fisco.bcos.web3j.protocol.Web3j web3j = Web3SDK2Wrapper.initWeb3j(gid, fiscoConfig);
            groups.put(gid, web3j);
        }
        log.info("all group in nodes: {}", groups.keySet());

        // deploy topic control contract for every group
        Map<Long, List<EchoAddress>> echoAddresses = new HashMap<>();
        for (Map.Entry<Long, org.fisco.bcos.web3j.protocol.Web3j> e : groups.entrySet()) {
            List<EchoAddress> groupAddress = new ArrayList<>();
            if (!dealOneGroup(e.getKey(), e.getValue(), credentials, groupAddress)) {
                return false;
            }
            echoAddresses.put(e.getKey(), groupAddress);
        }

        System.out.println(nowTime() + " topic control address in every group:");
        for (Map.Entry<Long, List<EchoAddress>> e : echoAddresses.entrySet()) {
            System.out.println("topic control address in group: " + e.getKey());
            for (EchoAddress address : e.getValue()) {
                System.out.println("\t" + address.toString());
            }
        }

        return true;
    }

    private static boolean dealOneGroup(Long groupId,
                                        org.fisco.bcos.web3j.protocol.Web3j web3j,
                                        org.fisco.bcos.web3j.crypto.Credentials credentials,
                                        List<EchoAddress> groupAddress) throws BrokerException {
        Map<Long, String> original = Web3SDK2Wrapper.listAddress(web3j, credentials);
        log.info("address list in CRUD groupId: {}, {}", groupId, original);

        // if nowVersion exist
        boolean exist = false;
        // highest version in CRUD
        Long highestVersion = 0L;
        for (Map.Entry<Long, String> topicControlAddress : original.entrySet()) {
            groupAddress.add(new EchoAddress(topicControlAddress.getKey(), topicControlAddress.getValue(), false));

            if (!SupportedVersion.history.contains(topicControlAddress.getKey())) {
                log.error("unknown solidity version in group: {} CRUD: {}", groupId, topicControlAddress.getKey());
                return false;
            }

            if (topicControlAddress.getKey() > highestVersion) {
                highestVersion = topicControlAddress.getKey();
            }

            if (SupportedVersion.nowVersion.equals(topicControlAddress.getKey())) {
                exist = true;
            }
        }

        if (exist) {
            log.info("find topic control address in now version groupId: {}, skip", groupId);
            return true;
        }

        // deploy topic control
        String topicControlAddress = Web3SDK2Wrapper.deployTopicControl(web3j, credentials);
        log.info("deploy topic control success, group: {} version: {} address: {}", groupId, SupportedVersion.nowVersion, topicControlAddress);

        // flush topic info from low into new version
        if (highestVersion > 0L && highestVersion < SupportedVersion.nowVersion) {
            System.out.println(String.format("flush topic info from low version, %d -> %d", highestVersion, SupportedVersion.nowVersion));
            boolean result = SupportedVersion.flushData(web3j, credentials, original, highestVersion, SupportedVersion.nowVersion);
            if (!result) {
                log.error("flush topic info data failed, {} -> {}", highestVersion, SupportedVersion.nowVersion);
                return false;
            }
        }

        // save topic control address into CRUD
        boolean result = Web3SDK2Wrapper.addAddress(web3j, credentials, SupportedVersion.nowVersion, topicControlAddress);
        log.info("save topic control address into CRUD, group: {} result: {}", groupId, result);
        if (result) {
            groupAddress.add(new EchoAddress(SupportedVersion.nowVersion, topicControlAddress, true));
        }

        return result;
    }

    private static boolean deployV1Contract(FiscoConfig fiscoConfig) throws BrokerException {
        org.bcos.web3j.crypto.Credentials credentials = Web3SDKWrapper.getCredentials(fiscoConfig);
        org.bcos.web3j.protocol.Web3j web3j = Web3SDKWrapper.initWeb3j(fiscoConfig);

        // check exist first
        String original = Web3SDKWrapper.getAddress(web3j, credentials, fiscoConfig.getProxyAddress());
        if (!StringUtils.isBlank(original)) {
            log.info("topic control address already exist, address: {}", original);
            System.out.println(nowTime() + " topic control address: " + original);
            return false;
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

        return true;
    }

    private static String nowTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(new Date());
    }

    private static void systemExit(int code) {
        System.out.flush();
        System.exit(code);
    }
}
