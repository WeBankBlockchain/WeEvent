package com.webank.weevent.broker.fabric.util;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.google.protobuf.InvalidProtocolBufferException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.hyperledger.fabric.protos.ledger.rwset.kvrwset.KvRwset;
import org.hyperledger.fabric.sdk.BlockEvent;
import org.hyperledger.fabric.sdk.BlockInfo;
import org.hyperledger.fabric.sdk.BlockchainInfo;
import org.hyperledger.fabric.sdk.ChaincodeID;
import org.hyperledger.fabric.sdk.ChaincodeResponse;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.InstallProposalRequest;
import org.hyperledger.fabric.sdk.InstantiateProposalRequest;
import org.hyperledger.fabric.sdk.Orderer;
import org.hyperledger.fabric.sdk.Peer;
import org.hyperledger.fabric.sdk.ProposalResponse;
import org.hyperledger.fabric.sdk.SDKUtils;
import org.hyperledger.fabric.sdk.TransactionRequest;
import org.hyperledger.fabric.sdk.TxReadWriteSetInfo;
import org.hyperledger.fabric.sdk.exception.CryptoException;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.hyperledger.fabric.sdk.exception.TransactionException;
import org.hyperledger.fabric.sdk.security.CryptoSuite;

import java.util.Properties;

import com.webank.weevent.broker.fabric.config.FabricConfig;

import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
public class FabricDeployContractUtil {
    public static FabricConfig fabricConfig = new FabricConfig();

    public static void main(String[] args) throws Exception {
        fabricConfig.load();
        try {
            HFClient client = initializeClient();
            Channel channel = initializeChannel(client, fabricConfig.getChannelName());
            //get Topic chaincodeID
            ChaincodeID chaincodeID = ChaincodeID.newBuilder().setName(fabricConfig.getTopicName()).setVersion(fabricConfig.getTopicVerison()).build();
            //install Topic chaincode
            Collection<ProposalResponse> proposalResponses = installProposal(client, channel, chaincodeID, TransactionRequest.Type.GO_LANG, fabricConfig.getTopicVerison(), fabricConfig.getTopicSourceLoc(), fabricConfig.getTopicPath());
            for (ProposalResponse response : proposalResponses) {
                if (response.getStatus() == ChaincodeResponse.Status.SUCCESS) {
                    log.debug("Install Topic SUCC Txid={}, peer={}", response.getTransactionID(), response.getPeer().getUrl());
                } else {
                    log.error("Install Topic FAIL Txid={}, peer={}", response.getMessage(), response.getTransactionID(), response.getPeer().getUrl());
                    return;
                }
            }
            //instant Topic chaincode
            BlockEvent.TransactionEvent transactionEvent = instantiateProposal(client, channel, chaincodeID, TransactionRequest.Type.GO_LANG);//Instantiate Topic chaincode
            if (!"".equals(transactionEvent.getTransactionID())) {
                log.debug("Instantiate Topic SUCC transactionEvent={}", transactionEvent);
            } else {
                log.error("Instantiate Topic FAIL transactionEvent={}", transactionEvent);
                return;
            }

            //get TopicController chaincodeID
            chaincodeID = ChaincodeID.newBuilder().setName(fabricConfig.getTopicControllerName()).setVersion(fabricConfig.getTopicControllerVersion()).build();
            //install TopicController chaincode
            proposalResponses = installProposal(client, channel, chaincodeID, TransactionRequest.Type.GO_LANG, fabricConfig.getTopicControllerVersion(), fabricConfig.getTopicControllerSourceLoc(), fabricConfig.getTopicControllerPath());//install TopicController chaincode
            for (ProposalResponse response : proposalResponses) {
                if (response.getStatus() == ChaincodeResponse.Status.SUCCESS) {
                    log.debug("Install TopicController SUCC Txid={}, peer={}", response.getTransactionID(), response.getPeer().getUrl());
                    //dumpRWSet(response);
                } else {
                    log.error("Install TopicController FAIL errorMsg={} Txid={}, peer={}", response.getMessage(), response.getTransactionID(), response.getPeer().getUrl());
                    return;
                }
            }

            //instant TopicController chaincode
            transactionEvent = instantiateProposal(client, channel, chaincodeID, TransactionRequest.Type.GO_LANG);
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

    // Create HFClient
    public static HFClient initializeClient() throws CryptoException, InvalidArgumentException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        HFClient client = HFClient.createNewInstance();
        client.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
        client.setUserContext(new FabricUser());
        return client;
    }


    // Create Channel
    public static Channel initializeChannel(HFClient client, String channelName) throws InvalidArgumentException, TransactionException {
        Properties orderer1Prop = new Properties();
        orderer1Prop.setProperty("pemFile", fabricConfig.getOrdererTlsCaFile());
        orderer1Prop.setProperty("sslProvider", "openSSL");
        orderer1Prop.setProperty("negotiationType", "TLS");
        orderer1Prop.setProperty("ordererWaitTimeMilliSecs", "300000");
        orderer1Prop.setProperty("hostnameOverride", "orderer");
        orderer1Prop.setProperty("trustServerCertificate", "true");
        orderer1Prop.setProperty("allowAllHostNames", "true");
        Orderer orderer1 = client.newOrderer("orderer", fabricConfig.getOrdererAddress(), orderer1Prop);

        Properties peer0Prop = new Properties();
        peer0Prop.setProperty("pemFile", fabricConfig.getPeerTlsCaFile());
        peer0Prop.setProperty("sslProvider", "openSSL");
        peer0Prop.setProperty("negotiationType", "TLS");
        peer0Prop.setProperty("hostnameOverride", "peer0");
        peer0Prop.setProperty("trustServerCertificate", "true");
        peer0Prop.setProperty("allowAllHostNames", "true");
        Peer peer0 = client.newPeer("peer0", fabricConfig.getPeerAddress(), peer0Prop);

        Channel channel = client.newChannel(channelName);
        channel.addOrderer(orderer1);
        channel.addPeer(peer0);
        channel.initialize();
        return channel;
    }

    private static Collection<ProposalResponse> installProposal(HFClient client, Channel channel, ChaincodeID chaincodeID,
                                                                TransactionRequest.Type chaincodeLang,         // Type.GO_LANG
                                                                String chaincodeVer,        // "v1"
                                                                String chaincodeSourceLoc,  // "/opt/gopath"
                                                                String chaincodePath        // "github.com/hyperledger/fabric/peer/chaincode/go/chaincode_example02
    ) throws InvalidArgumentException, ProposalException {
        InstallProposalRequest installProposalRequest = client.newInstallProposalRequest();
        installProposalRequest.setChaincodeID(chaincodeID);
        installProposalRequest.setChaincodeVersion(chaincodeVer);
        installProposalRequest.setChaincodeLanguage(chaincodeLang);
        installProposalRequest.setChaincodeSourceLocation(new File(chaincodeSourceLoc));
        installProposalRequest.setChaincodePath(chaincodePath);
        Collection<ProposalResponse> propResp = client.sendInstallProposal(installProposalRequest, channel.getPeers());
        return propResp;
    }

    private static String printableString(final String string) {
        int maxLogStringLength = 64;
        if (string == null || string.length() == 0) {
            return string;
        }

        String ret = string.replaceAll("[^\\p{Print}]", "?");
        ret = ret.substring(0, Math.min(ret.length(), maxLogStringLength)) + (ret.length() > maxLogStringLength ? "..." : "");
        return ret;
    }

    private static BlockEvent.TransactionEvent instantiateProposal(HFClient client, Channel channel, ChaincodeID chaincodeID, TransactionRequest.Type chaincodeLang) throws InvalidArgumentException, ProposalException, InterruptedException, ExecutionException, TimeoutException {
        InstantiateProposalRequest instantiateProposalRequest = client.newInstantiationProposalRequest();
        instantiateProposalRequest.setProposalWaitTime(120000);//time in milliseconds
        instantiateProposalRequest.setChaincodeID(chaincodeID);
        instantiateProposalRequest.setChaincodeLanguage(chaincodeLang);
        instantiateProposalRequest.setFcn("init");
        instantiateProposalRequest.setArgs(new String[]{});

        // I do not know the purpose of transient map works for.
        Map<String, byte[]> transientMap = new HashMap<>();
        transientMap.put("HyperLedgerFabric", "InstantiateProposalRequest:JavaSDK".getBytes(UTF_8));
        transientMap.put("method", "InstantiateProposalRequest".getBytes(UTF_8));
        instantiateProposalRequest.setTransientMap(transientMap);
        Collection<ProposalResponse> propResp = channel.sendInstantiationProposal(instantiateProposalRequest, channel.getPeers());
        BlockEvent.TransactionEvent transactionEvent = sendTransaction(channel, propResp);
        return transactionEvent;
    }

    private static BlockEvent.TransactionEvent sendTransaction(Channel channel, Collection<ProposalResponse> propResp) throws InvalidArgumentException, InterruptedException, ExecutionException, TimeoutException {
        List<ProposalResponse> successful = new LinkedList<ProposalResponse>();
        List<ProposalResponse> failed = new LinkedList<ProposalResponse>();
        for (ProposalResponse response : propResp) {
            if (response.getStatus() == ProposalResponse.Status.SUCCESS) {
                String payload = new String(response.getChaincodeActionResponsePayload());
                log.debug(String.format("[√] Got success response from peer {} => payload: {}", response.getPeer().getName(), payload));
                successful.add(response);
            } else {
                String status = response.getStatus().toString();
                String msg = response.getMessage();
                log.error(String.format("[×] Got failed response from peer{} => {}: {} ", response.getPeer().getName(), status, msg));
                failed.add(response);
            }
        }

        CompletableFuture<BlockEvent.TransactionEvent> carfuture = channel.sendTransaction(propResp);
        BlockEvent.TransactionEvent transactionEvent = carfuture.get(30, TimeUnit.SECONDS);
        return transactionEvent;
    }

    public static String executeChaincode(HFClient client, Channel channel, ChaincodeID chaincodeID, boolean invoke, String func, String... args) throws
            ProposalException, InvalidArgumentException, UnsupportedEncodingException, InterruptedException,
            ExecutionException, TimeoutException {
        ChaincodeExecuter executer = new ChaincodeExecuter();
        return executer.executeTransaction(client, channel, chaincodeID, invoke, func, args);
    }
}
