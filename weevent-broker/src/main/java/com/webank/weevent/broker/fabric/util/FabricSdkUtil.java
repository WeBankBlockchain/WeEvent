package com.webank.weevent.broker.fabric.util;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import com.google.protobuf.InvalidProtocolBufferException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.checkerframework.checker.units.qual.C;
import org.hyperledger.fabric.protos.ledger.rwset.kvrwset.KvRwset;
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
import org.hyperledger.fabric.sdk.exception.ChaincodeEndorsementPolicyParseException;
import org.hyperledger.fabric.sdk.exception.CryptoException;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.hyperledger.fabric.sdk.exception.TransactionException;
import org.hyperledger.fabric.sdk.security.CryptoSuite;

import java.util.Properties;

import com.webank.weevent.broker.fabric.config.FabricConfig;

import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
public class FabricSdkUtil {
    protected static FabricConfig fabricConfig = new FabricConfig();

    public static void main(String[] args) throws Exception {
        fabricConfig.load();
        try {
            HFClient client = initializeClient();
            Channel channel = initializeChannel(client, fabricConfig.getChannelName());
            ChaincodeID chaincodeID = ChaincodeID.newBuilder().setName(fabricConfig.getTopicName()).setVersion(fabricConfig.getTopicVerison()).build();
            Collection<ProposalResponse> proposalResponses = installProposal(client, channel, chaincodeID, TransactionRequest.Type.GO_LANG, fabricConfig.getTopicVerison(), fabricConfig.getTopicSourceLoc(), fabricConfig.getTopicPath());//install Topic chaincode
            for (ProposalResponse response : proposalResponses) {
                if (response.getStatus() == ChaincodeResponse.Status.SUCCESS) {
                    log.debug("Install Topic SUCC Txid={}, peer={}", response.getTransactionID(), response.getPeer().getUrl());
                    //dumpRWSet(response);
                } else {
                    log.error("Install Topic FAIL Txid={}, peer={}", response.getMessage(), response.getTransactionID(), response.getPeer().getUrl());
                    //return;
                }
            }

            proposalResponses = instantiateProposal(client, channel, chaincodeID, TransactionRequest.Type.GO_LANG);//Instantiate Topic chaincode
            for (ProposalResponse response : proposalResponses) {
                if (response.getStatus() == ChaincodeResponse.Status.SUCCESS) {
                    log.debug("Instantiate Topic SUCC Txid={}, peer={}", response.getTransactionID(), response.getPeer().getUrl());
                    //dumpRWSet(response);
                } else {
                    log.error("Instantiate Topic FAIL errorMsg={} Txid={}, peer={}", response.getMessage(), response.getTransactionID(), response.getPeer().getUrl());
                    //return;
                }
            }

            chaincodeID = ChaincodeID.newBuilder().setName(fabricConfig.getTopicControllerName()).setVersion(fabricConfig.getTopicControllerVersion()).build();
            proposalResponses = installProposal(client, channel, chaincodeID, TransactionRequest.Type.GO_LANG, fabricConfig.getTopicControllerVersion(), fabricConfig.getTopicControllerSourceLoc(), fabricConfig.getTopicControllerPath());//install TopicController chaincode
            for (ProposalResponse response : proposalResponses) {
                if (response.getStatus() == ChaincodeResponse.Status.SUCCESS) {
                    log.debug("Install TopicController SUCC Txid={}, peer={}", response.getTransactionID(), response.getPeer().getUrl());
                    //dumpRWSet(response);
                } else {
                    log.error("Install TopicController FAIL errorMsg={} Txid={}, peer={}", response.getMessage(), response.getTransactionID(), response.getPeer().getUrl());
                    //return;
                }
            }

            proposalResponses = instantiateProposal(client, channel, chaincodeID, TransactionRequest.Type.GO_LANG);//Instantiate TopicController chaincode
            for (ProposalResponse response : proposalResponses) {
                if (response.getStatus() == ChaincodeResponse.Status.SUCCESS) {
                    log.debug("Instantiate TopicController SUCC Txid={}, peer={}", response.getTransactionID(), response.getPeer().getUrl());
                    //dumpRWSet(response);
                } else {
                    log.error("Instantiate TopicController FAIL errorMsg={} Txid={}, peer={}", response.getMessage(), response.getTransactionID(), response.getPeer().getUrl());
                    //return;
                }
            }

            //set topicName to topicController
            executeChaincode(client, channel, chaincodeID, true, "addTopicContractName", "Topic");
            //set topicName from topicController
            executeChaincode(client, channel, chaincodeID, false, "getTopicContractName");

            //printChannelInfo(client, channel);
            log.info("Shutdown channel.");
            channel.shutdown(true);
        } catch (Exception e) {
            log.error("exception", e);
        }
    }

    // Create HFClient
    private static HFClient initializeClient() throws CryptoException, InvalidArgumentException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        HFClient client = HFClient.createNewInstance();
        client.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
        client.setUserContext(new FabricUser());
        return client;
    }


    // Create Channel
    private static Channel initializeChannel(HFClient client, String channelName) throws InvalidArgumentException, TransactionException {
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

    private static void dumpRWSet(ProposalResponse response) {
        try {
            for (TxReadWriteSetInfo.NsRwsetInfo nsRwsetInfo : response.getChaincodeActionResponseReadWriteSetInfo().getNsRwsetInfos()) {
                String namespace = nsRwsetInfo.getNamespace();
                KvRwset.KVRWSet rws = nsRwsetInfo.getRwset();

                int rsid = -1;
                for (KvRwset.KVRead readList : rws.getReadsList()) {
                    rsid++;
                    log.debug("Namespace %s read  set[%d]: key[%s]=version[%d:%d]", namespace, rsid, readList.getKey(), readList.getVersion().getBlockNum(), readList.getVersion().getTxNum());
                }

                rsid = -1;
                for (KvRwset.KVWrite writeList : rws.getWritesList()) {
                    rsid++;
                    String valAsString = printableString(new String(writeList.getValue().toByteArray(), "UTF-8"));
                    log.debug("Namespace %s write set[%d]: key[%s]=value[%s]", namespace, rsid, writeList.getKey(), valAsString);
                }
            }
        } catch (InvalidArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvalidProtocolBufferException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private static Collection<ProposalResponse> instantiateProposal(HFClient client, Channel channel, ChaincodeID chaincodeID, TransactionRequest.Type chaincodeLang) throws InvalidArgumentException, ChaincodeEndorsementPolicyParseException, IOException, ProposalException {
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
        return propResp;
    }

    private static void executeChaincode(HFClient client, Channel channel, ChaincodeID chaincodeID, boolean invoke, String func, String... args) throws
            ProposalException, InvalidArgumentException, UnsupportedEncodingException, InterruptedException,
            ExecutionException, TimeoutException {
        ChaincodeExecuter executer = new ChaincodeExecuter();
        executer.executeTransaction(client, channel, chaincodeID, invoke, func, args);
    }

    private static void printChannelInfo(HFClient client, Channel channel) throws
            ProposalException, InvalidArgumentException, IOException {
        BlockchainInfo channelInfo = channel.queryBlockchainInfo();

        log.info("Channel height: " + channelInfo.getHeight());
        for (long current = channelInfo.getHeight() - 1; current > -1; --current) {
            BlockInfo returnedBlock = channel.queryBlockByNumber(current);
            final long blockNumber = returnedBlock.getBlockNumber();

            log.info(String.format("Block #%d has previous hash id: %s", blockNumber, Hex.encodeHexString(returnedBlock.getPreviousHash())));
            log.info(String.format("Block #%d has data hash: %s", blockNumber, Hex.encodeHexString(returnedBlock.getDataHash())));
            log.info(String.format("Block #%d has calculated block hash is %s",
                    blockNumber, Hex.encodeHexString(SDKUtils.calculateBlockHash(client, blockNumber, returnedBlock.getPreviousHash(), returnedBlock.getDataHash()))));
        }
    }
}
