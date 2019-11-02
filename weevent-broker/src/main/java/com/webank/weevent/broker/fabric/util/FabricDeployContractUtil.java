package com.webank.weevent.broker.fabric.util;

import java.util.Collection;
import java.util.List;

import com.webank.weevent.broker.fabric.config.FabricConfig;
import com.webank.weevent.broker.fabric.sdk.FabricSDKWrapper;

import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hyperledger.fabric.sdk.BlockEvent;
import org.hyperledger.fabric.sdk.ChaincodeID;
import org.hyperledger.fabric.sdk.ChaincodeResponse;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.ProposalResponse;
import org.hyperledger.fabric.sdk.TransactionRequest;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;

/**
 * @author websterchen
 * @version v1.1
 * @since 2019/8/6
 */
@Slf4j
public class FabricDeployContractUtil {
    public static FabricConfig fabricConfig = new FabricConfig();

    public static void main(String[] args) {
        fabricConfig.load();

        try {
            HFClient client = FabricSDKWrapper.initializeClient(fabricConfig);
            Channel channel = FabricSDKWrapper.initializeChannel(client, fabricConfig.getChannelName(), fabricConfig);

            // Check if the chain code is already installed
            if (!checkChaincodeIfInstalled(client, fabricConfig)){
                // get Topic chaincodeID
                ChaincodeID chaincodeID = FabricSDKWrapper.getChainCodeID(fabricConfig.getTopicName(), fabricConfig.getTopicVerison());
                // install Topic chaincode
                Collection<ProposalResponse> proposalResponses = FabricSDKWrapper.installProposal(client, channel, chaincodeID, TransactionRequest.Type.GO_LANG, fabricConfig.getTopicVerison(), fabricConfig.getTopicSourceLoc(), fabricConfig.getTopicPath());
                for (ProposalResponse response : proposalResponses) {
                    if (response.getStatus() == ChaincodeResponse.Status.SUCCESS) {
                        log.debug("Install Topic SUCC Txid={}, peer={}", response.getTransactionID(), response.getPeer().getUrl());
                    } else {
                        log.error("Install Topic FAIL, errorMsg={}, Txid={}, peer={}", response.getMessage(), response.getTransactionID(), response.getPeer().getUrl());
                        systemExit(1);
                    }
                }

                // instant Topic chaincode
                proposalResponses = FabricSDKWrapper.instantiateProposal(client, channel, chaincodeID, TransactionRequest.Type.GO_LANG, fabricConfig.getProposalTimeout());
                BlockEvent.TransactionEvent transactionEvent = FabricSDKWrapper.sendTransaction(channel, proposalResponses, fabricConfig.getTransactionTimeout());
                if (!StringUtils.isBlank(transactionEvent.getTransactionID())) {
                    log.debug("Instantiate Topic SUCC transactionEvent={}", transactionEvent);
                } else {
                    log.error("Instantiate Topic FAIL transactionEvent={}", transactionEvent);
                    systemExit(1);
                }

                // get TopicController chaincodeID
                chaincodeID = FabricSDKWrapper.getChainCodeID(fabricConfig.getTopicControllerName(), fabricConfig.getTopicControllerVersion());
                // install TopicController chaincode
                proposalResponses = FabricSDKWrapper.installProposal(client, channel, chaincodeID, TransactionRequest.Type.GO_LANG, fabricConfig.getTopicControllerVersion(), fabricConfig.getTopicControllerSourceLoc(), fabricConfig.getTopicControllerPath());
                for (ProposalResponse response : proposalResponses) {
                    if (response.getStatus() == ChaincodeResponse.Status.SUCCESS) {
                        log.debug("Install TopicController SUCC Txid={}, peer={}", response.getTransactionID(), response.getPeer().getUrl());
                    } else {
                        log.error("Install TopicController FAIL errorMsg={} Txid={}, peer={}", response.getMessage(), response.getTransactionID(), response.getPeer().getUrl());
                        systemExit(1);
                    }
                }

                // instant TopicController chaincode
                proposalResponses = FabricSDKWrapper.instantiateProposal(client, channel, chaincodeID, TransactionRequest.Type.GO_LANG, fabricConfig.getProposalTimeout());
                transactionEvent = FabricSDKWrapper.sendTransaction(channel, proposalResponses, fabricConfig.getTransactionTimeout());
                if (!StringUtils.isBlank(transactionEvent.getTransactionID())) {
                    log.debug("Instantiate TopicController SUCC transactionEvent:{}", transactionEvent);
                } else {
                    log.error("Instantiate TopicController FAIL transactionEvent:{}", transactionEvent);
                    systemExit(1);
                }
            }

            log.info("Shutdown channel.");
            channel.shutdown(true);
        } catch (Exception e) {
            log.error("exception", e);
            systemExit(1);
        }
    }

    private static void systemExit(int code) {
        System.out.flush();
        System.exit(code);
    }

    private static boolean checkChaincodeIfInstalled(HFClient client, FabricConfig fabricConfig) throws InvalidArgumentException, ProposalException {
        boolean isInstalled = false;
        String topicName = fabricConfig.getTopicName();
        String topicVersion = fabricConfig.getTopicVerison();
        String topicControllerName = fabricConfig.getTopicControllerName();
        String topicControllerVersion = fabricConfig.getTopicControllerVersion();

        List<Pair<String, String>> chaincodes = FabricSDKWrapper.queryInstalledChaincodes(client, FabricSDKWrapper.getPeer(client, fabricConfig));

        for (Pair<String, String> chaincode : chaincodes) {
            if (topicName.equals(chaincode.getKey()) && topicVersion.equals(chaincode.getValue())){
                log.info("chaincode topic={}:{} already Installed.", topicName, topicVersion);
                isInstalled = true;
                break;
            }
            if (topicControllerName.equals(chaincode.getKey()) && topicControllerVersion.equals(chaincode.getValue())) {
                log.info("chaincode topicController={}:{} already Installed.", topicControllerName, topicControllerVersion);
                isInstalled = true;
                break;
            }
        }
        return isInstalled;
    }

}
