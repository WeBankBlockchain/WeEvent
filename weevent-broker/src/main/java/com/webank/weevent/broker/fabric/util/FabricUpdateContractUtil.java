package com.webank.weevent.broker.fabric.util;

import lombok.extern.slf4j.Slf4j;
import org.hyperledger.fabric.sdk.ChaincodeID;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.HFClient;

import static com.webank.weevent.broker.fabric.util.FabricDeployContractUtil.executeChaincode;
import static com.webank.weevent.broker.fabric.util.FabricDeployContractUtil.fabricConfig;
import static com.webank.weevent.broker.fabric.util.FabricDeployContractUtil.initializeChannel;
import static com.webank.weevent.broker.fabric.util.FabricDeployContractUtil.initializeClient;

@Slf4j
public class FabricUpdateContractUtil {

    public static void main(String[] args) throws Exception {
        fabricConfig.load();
        HFClient client = initializeClient();
        Channel channel = initializeChannel(client, fabricConfig.getChannelName());
        ChaincodeID chaincodeID = ChaincodeID.newBuilder().setName(fabricConfig.getTopicControllerName()).setVersion(fabricConfig.getTopicControllerVersion()).build();
        switch (args[0]) {
            case "add":
                executeChaincode(client, channel, chaincodeID, true, "addTopicContractName", fabricConfig.getTopicName(), fabricConfig.getTopicVerison());
                String topicContractNameAdd = executeChaincode(client, channel, chaincodeID, false, "getTopicContractName");
                String topicContractVersionAdd = executeChaincode(client, channel, chaincodeID, false, "getTopicContractVersion");
                if (fabricConfig.getTopicName().equals(topicContractNameAdd) && fabricConfig.getTopicVerison().equals(topicContractVersionAdd)) {
                    log.debug("add TopicContract success");
                } else {
                    log.error("add TopicContrct error");
                }
                break;
            case "update":
                executeChaincode(client, channel, chaincodeID, true, "updateTopicContractName", fabricConfig.getTopicName(), fabricConfig.getTopicVerison());
                String topicContractNameUpdate = executeChaincode(client, channel, chaincodeID, false, "getTopicContractName");
                String topicContractVersionUpdate = executeChaincode(client, channel, chaincodeID, false, "getTopicContractVersion");
                if (fabricConfig.getTopicName().equals(topicContractNameUpdate) && fabricConfig.getTopicVerison().equals(topicContractVersionUpdate)) {
                    log.debug("update TopicContract success");
                } else {
                    log.error("update TopicContrct error");
                }
                break;
        }
    }
}
