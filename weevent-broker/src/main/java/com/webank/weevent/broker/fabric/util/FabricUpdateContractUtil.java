package com.webank.weevent.broker.fabric.util;

import com.webank.weevent.broker.fabric.sdk.FabricSDKWrapper;

import lombok.extern.slf4j.Slf4j;
import org.hyperledger.fabric.sdk.ChaincodeID;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.HFClient;

import static com.webank.weevent.broker.fabric.util.FabricDeployContractUtil.fabricConfig;

/**
 * @author websterchen
 * @version v1.1
 * @since 2019/8/6
 */
@Slf4j
public class FabricUpdateContractUtil {

    public static void main(String[] args) throws Exception {
        fabricConfig.load();

        HFClient client = FabricSDKWrapper.initializeClient(fabricConfig);
        Channel channel = FabricSDKWrapper.initializeChannel(client, fabricConfig.getChannelName(), fabricConfig);
        ChaincodeID chaincodeID = ChaincodeID.newBuilder().setName(fabricConfig.getTopicControllerName()).setVersion(fabricConfig.getTopicControllerVersion()).build();
        switch (args[0]) {
            case "add":
                FabricSDKWrapper.executeTransaction(client, channel, chaincodeID, true, "addTopicContractName", fabricConfig.getTransactionTimeout(), fabricConfig.getTopicName(), fabricConfig.getTopicVerison());
                String topicContractNameAdd = FabricSDKWrapper.executeTransaction(client, channel, chaincodeID, false, "getTopicContractName", fabricConfig.getTransactionTimeout()).getPayLoad();
                String topicContractVersionAdd = FabricSDKWrapper.executeTransaction(client, channel, chaincodeID, false, "getTopicContractVersion", fabricConfig.getTransactionTimeout()).getPayLoad();
                if (fabricConfig.getTopicName().equals(topicContractNameAdd) && fabricConfig.getTopicVerison().equals(topicContractVersionAdd)) {
                    log.debug("add TopicContract success");
                } else {
                    log.error("add TopicContrct error");
                    systemExit(1);
                }
                break;
            case "update":
                FabricSDKWrapper.executeTransaction(client, channel, chaincodeID, true, "updateTopicContractName", fabricConfig.getTransactionTimeout(), fabricConfig.getTopicName(), fabricConfig.getTopicVerison());
                String topicContractNameUpdate = FabricSDKWrapper.executeTransaction(client, channel, chaincodeID, false, "getTopicContractName", fabricConfig.getTransactionTimeout()).getPayLoad();
                String topicContractVersionUpdate = FabricSDKWrapper.executeTransaction(client, channel, chaincodeID, false, "getTopicContractVersion", fabricConfig.getTransactionTimeout()).getPayLoad();
                if (fabricConfig.getTopicName().equals(topicContractNameUpdate) && fabricConfig.getTopicVerison().equals(topicContractVersionUpdate)) {
                    log.debug("update TopicContract success");
                } else {
                    log.error("update TopicContrct error");
                    systemExit(1);
                }
                break;
        }
    }

    private static void systemExit(int code) {
        System.out.flush();
        System.exit(code);
    }
}
