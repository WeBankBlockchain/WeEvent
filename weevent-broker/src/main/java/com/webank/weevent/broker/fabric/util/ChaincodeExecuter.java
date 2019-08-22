/*
 *  Copyright 2018 Aliyun.com All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.webank.weevent.broker.fabric.util;

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import lombok.extern.slf4j.Slf4j;
import org.hyperledger.fabric.sdk.BlockEvent;
import org.hyperledger.fabric.sdk.ChaincodeID;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.ProposalResponse;
import org.hyperledger.fabric.sdk.TransactionProposalRequest;
import org.hyperledger.fabric.sdk.TransactionRequest;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;

@Slf4j
public class ChaincodeExecuter {
    public void executeTransaction(HFClient client, Channel channel, ChaincodeID chaincodeID, boolean invoke, String func, String... args) throws InvalidArgumentException, ProposalException, UnsupportedEncodingException, InterruptedException, ExecutionException, TimeoutException {
        TransactionProposalRequest transactionProposalRequest = client.newTransactionProposalRequest();
        transactionProposalRequest.setChaincodeID(chaincodeID);
        transactionProposalRequest.setChaincodeLanguage(TransactionRequest.Type.GO_LANG);

        transactionProposalRequest.setFcn(func);
        transactionProposalRequest.setArgs(args);
        transactionProposalRequest.setProposalWaitTime(10000);


        List<ProposalResponse> successful = new LinkedList<ProposalResponse>();
        List<ProposalResponse> failed = new LinkedList<ProposalResponse>();
        // there is no need to retry. If not, you should re-send the transaction proposal.
        Collection<ProposalResponse> transactionPropResp = channel.sendTransactionProposal(transactionProposalRequest, channel.getPeers());
        for (ProposalResponse response : transactionPropResp) {
            if (response.getStatus() == ProposalResponse.Status.SUCCESS) {
                String payload = new String(response.getChaincodeActionResponsePayload());
                log.info(String.format("[√] Got success response from peer {} => payload: {}", response.getPeer().getName(), payload));
                successful.add(response);
            } else {
                String status = response.getStatus().toString();
                String msg = response.getMessage();
                log.warn(String.format("[×] Got failed response from peer{} => {}: {} ", response.getPeer().getName(), status, msg));
                failed.add(response);
            }
        }

        if (invoke) {
            log.info("Sending transaction to orderers...");
            // Java sdk tries all orderers to send transaction, so don't worry about one orderer gone.
            /*channel.sendTransaction(successful).thenApply(transactionEvent -> {
                logger.info("Orderer response: txid" + transactionEvent.getTransactionID());
                logger.info("Orderer response: block number: " + transactionEvent.getBlockEvent().getBlockNumber());
                return null;
            }).exceptionally(e -> {
                logger.error("Orderer exception happened: ", e);
                return null;
            }).get(waitTime, TimeUnit.SECONDS);*/

            CompletableFuture<BlockEvent.TransactionEvent> carfuture = channel.sendTransaction(successful);
            BlockEvent.TransactionEvent transactionEvent = carfuture.get(30, TimeUnit.SECONDS);
            log.debug("Wait event return: " + transactionEvent.getChannelId() + " " + transactionEvent.getTransactionID() + " " + transactionEvent.getType() + " " + transactionEvent.getValidationCode());
        }
    }
}
