package com.webank.weevent.broker.fisco.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.webank.weevent.broker.fisco.service.BaseService;
import com.webank.weevent.broker.fisco.util.Web3sdkUtils;
import com.webank.weevent.sdk.BrokerException;

import lombok.extern.slf4j.Slf4j;
import org.bcos.web3j.protocol.core.Request;
import org.bcos.web3j.protocol.core.methods.response.EthPeers;

/**
 * fisco-bcos block chain operator
 *
 * @author websterchen
 * @version 1.0
 * @since 2018/12/18
 */
@Slf4j
public class BlockChainServiceImpl extends BaseService {
    public BlockChainServiceImpl() {
        if (null == web3j || null == credentials) {
            if (!loadConfig()) {
                log.error("init web3sdk fail");
                throw new RuntimeException("init web3sdk fail");
            }
        }
    }

    public Map<String, List<String>> getNodeInfo() throws IOException {
        Request<?, EthPeers> ethPeersRequest = web3j.getAdminPeers();
        EthPeers ethPeers = ethPeersRequest.send();
        List<EthPeers.Peers> peerIpList = ethPeers.getAdminPeers();
        List<String> nodeIpList = new ArrayList<>();
        List<String> nodeIdList = new ArrayList<>();
        Map<String, List<String>> stringListMap = new HashMap<>();
        for (EthPeers.Peers peer : peerIpList) {
            String nodeIp = peer.getNetwork().values().toString();
            String nodeId = peer.getId();
            nodeIpList.add(nodeIp);
            nodeIdList.add(nodeId);
        }
        stringListMap.put("nodeId", nodeIdList);
        stringListMap.put("nodeIp", nodeIpList);
        return stringListMap;
    }

    public String deployTopicContracts() throws BrokerException {
        return Web3sdkUtils.deployTopicControl(web3j, credentials);
    }
}
