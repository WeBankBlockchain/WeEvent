package com.webank.weevent.broker.fabric.util;

import java.util.Collection;


import lombok.extern.slf4j.Slf4j;
import org.hyperledger.fabric.sdk.BlockEvent;
import org.hyperledger.fabric.sdk.ChaincodeID;
import org.hyperledger.fabric.sdk.ChaincodeResponse;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.ProposalResponse;
import org.hyperledger.fabric.sdk.TransactionRequest;

import com.webank.weevent.broker.fabric.config.FabricConfig;
import com.webank.weevent.broker.fabric.sdk.FabricSDKWrapper;

/**
 * @author websterchen
 * @version v1.1
 * @since 2019/8/6
 */
@Slf4j
public class FabricDeployContractUtil {
    public static FabricConfig fabricConfig = new FabricConfig();

    public static void main(String[] args) throws Exception {
        fabricConfig.load();
        try {
            HFClient client = FabricSDKWrapper.initializeClient(fabricConfig);
            Channel channel = FabricSDKWrapper.initializeChannel(client, fabricConfig.getChannelName(), fabricConfig);
            //get Topic chaincodeID
            ChaincodeID chaincodeID = FabricSDKWrapper.getChainCodeID(fabricConfig.getTopicName(), fabricConfig.getTopicVerison());
            //install Topic chaincode
            Collection<ProposalResponse> proposalResponses = FabricSDKWrapper.installProposal(client, channel, chaincodeID, TransactionRequest.Type.GO_LANG, fabricConfig.getTopicVerison(), fabricConfig.getTopicSourceLoc(), fabricConfig.getTopicPath());
            for (ProposalResponse response : proposalResponses) {
                if (response.getStatus() == ChaincodeResponse.Status.SUCCESS) {
                    log.debug("Install Topic SUCC Txid={}, peer={}", response.getTransactionID(), response.getPeer().getUrl());
                } else {
                    log.error("Install Topic FAIL Txid={}, peer={}", response.getMessage(), response.getTransactionID(), response.getPeer().getUrl());
                    return;
                }
            }
            //instant Topic chaincode
            proposalResponses = FabricSDKWrapper.instantiateProposal(client, channel, chaincodeID, TransactionRequest.Type.GO_LANG, fabricConfig.getProposalTimeout());//Instantiate Topic chaincode
            BlockEvent.TransactionEvent transactionEvent = FabricSDKWrapper.sendTransaction(channel, proposalResponses, fabricConfig.getTransactionTimeout());
            if (!"".equals(transactionEvent.getTransactionID())) {
                log.debug("Instantiate Topic SUCC transactionEvent={}", transactionEvent);
            } else {
                log.error("Instantiate Topic FAIL transactionEvent={}", transactionEvent);
                return;
            }

            //get TopicController chaincodeID
            chaincodeID = FabricSDKWrapper.getChainCodeID(fabricConfig.getTopicControllerName(), fabricConfig.getTopicControllerVersion());
            //install TopicController chaincode
            proposalResponses = FabricSDKWrapper.installProposal(client, channel, chaincodeID, TransactionRequest.Type.GO_LANG, fabricConfig.getTopicControllerVersion(), fabricConfig.getTopicControllerSourceLoc(), fabricConfig.getTopicControllerPath());//install TopicController chaincode
            for (ProposalResponse response : proposalResponses) {
                if (response.getStatus() == ChaincodeResponse.Status.SUCCESS) {
                    log.debug("Install TopicController SUCC Txid={}, peer={}", response.getTransactionID(), response.getPeer().getUrl());
                } else {
                    log.error("Install TopicController FAIL errorMsg={} Txid={}, peer={}", response.getMessage(), response.getTransactionID(), response.getPeer().getUrl());
                    return;
                }
            }

            //instant TopicController chaincode
            proposalResponses = FabricSDKWrapper.instantiateProposal(client, channel, chaincodeID, TransactionRequest.Type.GO_LANG, fabricConfig.getProposalTimeout());
            transactionEvent = FabricSDKWrapper.sendTransaction(channel, proposalResponses, fabricConfig.getTransactionTimeout());
            if (!"".equals(transactionEvent.getTransactionID())) {
                log.debug("Instantiate TopicController SUCC transactionEvent={}", transactionEvent);
            } else {
                log.error("Instantiate TopicController FAIL transactionEvent={}", transactionEvent);
                return;
            }
            log.info("Shutdown channel.");
            channel.shutdown(true);
        } catch (Exception e) {
            log.error("exception", e);
        }
    }
}
