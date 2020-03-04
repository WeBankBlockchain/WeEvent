package com.webank.weevent.broker.protocol.rest;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.webank.weevent.broker.config.BuildInfo;
import com.webank.weevent.client.BrokerException;
import com.webank.weevent.client.ErrorCode;
import com.webank.weevent.core.IConsumer;
import com.webank.weevent.core.dto.ContractContext;
import com.webank.weevent.core.dto.GroupGeneral;
import com.webank.weevent.core.dto.ListPage;
import com.webank.weevent.core.dto.QueryEntity;
import com.webank.weevent.core.dto.SubscriptionInfo;
import com.webank.weevent.core.dto.TbBlock;
import com.webank.weevent.core.dto.TbNode;
import com.webank.weevent.core.dto.TbTransHash;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * @author websterchen
 * @version 1.0
 * @since 2018/12/14
 */
@RequestMapping(value = "/admin")
@RestController
@Slf4j
public class AdminRest {
    private IConsumer consumer;
    private BuildInfo buildInfo;
    private DiscoveryClient discoveryClient;

    @Autowired
    public void setConsumer(IConsumer consumer) {
        this.consumer = consumer;
    }

    @Autowired
    public void setBuildInfo(BuildInfo buildInfo) {
        this.buildInfo = buildInfo;
    }

    @Autowired
    public void setDiscoveryClient(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    @RequestMapping(path = "/listGroup")
    public ResponseData<List<String>> listGroup() throws BrokerException {
        ResponseData<List<String>> responseData = new ResponseData<>();
        responseData.setData(this.consumer.listGroupId());
        responseData.setErrorCode(ErrorCode.SUCCESS);
        return responseData;
    }

    @RequestMapping(path = "/listNodes")
    public ResponseData<List<String>> listNodes() throws BrokerException {
        log.info("query node list");

        ResponseData<List<String>> responseData = new ResponseData<>();
        List<String> nodesInfo = new ArrayList<>();
        List<ServiceInstance> instances = this.discoveryClient.getInstances("weevent-broker");
        for (ServiceInstance serviceInstance : instances) {
            nodesInfo.add(serviceInstance.getUri().toString());
        }
        responseData.setErrorCode(ErrorCode.SUCCESS);
        responseData.setData(nodesInfo);
        return responseData;
    }

    @RequestMapping(path = "/listSubscription")
    public ResponseData<Map<String, SubscriptionInfo>> listSubscription(@RequestParam(name = "nodeIp") String nodeIp,
                                                                        @RequestParam(name = "groupId", required = false) String groupId) throws BrokerException {
        log.info("groupId:{}, nodeIp:{}", groupId, nodeIp);

        if (StringUtils.isBlank(nodeIp)) {
            log.error("node ipList is null.");
            throw new BrokerException("node ipList is null.");
        }

        Map<String, SubscriptionInfo> nodesInfo = new HashMap<>();
        try {
            log.info("zookeeper ip List:{}", nodeIp);
            String[] ipList = nodeIp.split(",");
            for (String ipStr : ipList) {
                if (!StringUtils.isBlank(ipStr)) {
                    SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
                    RestTemplate rest = new RestTemplate(requestFactory);
                    String url = ipStr + "/weevent-broker/admin/innerListSubscription?groupId=" + groupId;
                    log.info("url:{}", url);

                    ResponseEntity<SubscriptionInfo> rsp = rest.getForEntity(url, SubscriptionInfo.class);
                    log.debug("innerListSubscription:{}", rsp.getBody());
                    nodesInfo.put(nodeIp, rsp.getBody());
                }
            }
        } catch (Exception e) {
            log.error("find subscriptionList fail", e);
            throw new BrokerException("find subscriptionList fail", e);
        }

        ResponseData<Map<String, SubscriptionInfo>> responseData = new ResponseData<>();
        responseData.setErrorCode(ErrorCode.SUCCESS);
        responseData.setData(nodesInfo);
        return responseData;
    }

    @RequestMapping(path = "/innerListSubscription")
    public Map<String, SubscriptionInfo> innerListSubscription(@RequestParam(name = "groupId") String groupId) throws BrokerException {
        log.info("groupId:{}", groupId);

        return this.consumer.listSubscription(groupId);
    }

    @RequestMapping(path = "/getVersion")
    public ResponseData<BuildInfo> getVersion() {
        ResponseData<BuildInfo> responseData = new ResponseData<>();
        responseData.setErrorCode(ErrorCode.SUCCESS);
        responseData.setData(this.buildInfo);
        return responseData;
    }

    /**
     * get general
     */
    @RequestMapping(path = "/group/general")
    public ResponseData<GroupGeneral> getGroupGeneral(@RequestParam(value = "groupId", required = false) String groupId) throws BrokerException {
        log.info("groupId:{}", groupId);

        ResponseData<GroupGeneral> responseData = new ResponseData<>();
        GroupGeneral groupGeneral = this.consumer.getGroupGeneral(groupId);
        responseData.setCode(ErrorCode.SUCCESS.getCode());
        responseData.setMessage(ErrorCode.SUCCESS.getCodeDesc());
        responseData.setData(groupGeneral);
        return responseData;
    }


    /**
     * query transaction list.
     */
    @RequestMapping(path = "/transaction/transList")
    public ResponseData<ListPage<TbTransHash>> queryTransList(@RequestParam(value = "groupId", required = false) String groupId,
                                                              @RequestParam("pageNumber") Integer pageNumber,
                                                              @RequestParam("pageSize") Integer pageSize,
                                                              @RequestParam(value = "transactionHash", required = false) String transHash,
                                                              @RequestParam(value = "blockNumber", required = false) BigInteger blockNumber)
            throws BrokerException {
        log.info("groupId:{} pageNumber:{} pageSize:{} pkHash:{} blockNumber:{}", groupId, pageNumber, pageSize, transHash, blockNumber);

        ResponseData<ListPage<TbTransHash>> responseData = new ResponseData<>();
        QueryEntity queryEntity = new QueryEntity(groupId, pageNumber, pageSize, transHash, blockNumber);

        ListPage<TbTransHash> tbTransHashes = this.consumer.queryTransList(queryEntity);
        responseData.setCode(ErrorCode.SUCCESS.getCode());
        responseData.setMessage(ErrorCode.SUCCESS.getCodeDesc());
        responseData.setData(tbTransHashes);
        return responseData;
    }


    /**
     * query block list.
     */
    @RequestMapping(path = "/block/blockList")
    public ResponseData<ListPage<TbBlock>> queryBlockList(@RequestParam(value = "groupId", required = false) String groupId,
                                                          @RequestParam("pageNumber") Integer pageNumber,
                                                          @RequestParam("pageSize") Integer pageSize,
                                                          @RequestParam(value = "pkHash", required = false) String pkHash,
                                                          @RequestParam(value = "blockNumber", required = false) BigInteger blockNumber)
            throws BrokerException {
        log.info("groupId:{} pageNumber:{} pageSize:{} pkHash:{} blockNumber:{}", groupId, pageNumber, pageSize, pkHash, blockNumber);

        ResponseData<ListPage<TbBlock>> responseData = new ResponseData<>();
        QueryEntity queryEntity = new QueryEntity(groupId, pageNumber, pageSize, pkHash, blockNumber);

        ListPage<TbBlock> tbBlocks = this.consumer.queryBlockList(queryEntity);
        responseData.setCode(ErrorCode.SUCCESS.getCode());
        responseData.setMessage(ErrorCode.SUCCESS.getCodeDesc());
        responseData.setData(tbBlocks);
        return responseData;
    }

    /**
     * query node info list.
     */
    @RequestMapping(path = "/node/nodeList")
    public ResponseData<ListPage<TbNode>> queryNodeList(@RequestParam(value = "groupId", required = false) String groupId,
                                                        @RequestParam("pageNumber") Integer pageNumber,
                                                        @RequestParam("pageSize") Integer pageSize,
                                                        @RequestParam(value = "nodeName", required = false) String nodeName)
            throws BrokerException {
        log.info("groupId:{}  pageNumber:{} pageSize:{} nodeName:{}", groupId, pageNumber, pageSize, nodeName);

        QueryEntity queryEntity = new QueryEntity(groupId, pageNumber, pageSize, null, null);
        queryEntity.setNodeName(nodeName);
        ResponseData<ListPage<TbNode>> responseData = new ResponseData<>();
        responseData.setCode(ErrorCode.SUCCESS.getCode());
        responseData.setMessage(ErrorCode.SUCCESS.getCodeDesc());
        ListPage<TbNode> tbNodeListPage = this.consumer.queryNodeList(queryEntity);
        tbNodeListPage.setPageIndex(pageNumber);
        tbNodeListPage.setPageSize(pageSize);
        responseData.setData(tbNodeListPage);
        return responseData;
    }

    /**
     * query ContractContext.
     */
    @RequestMapping(path = "/getContractContext")
    public ResponseData<ContractContext> getContractContext(@RequestParam(value = "groupId", required = false) String groupId)
            throws BrokerException {
        log.info("groupId:{} ", groupId);

        ResponseData<ContractContext> responseData = new ResponseData<>();
        responseData.setCode(ErrorCode.SUCCESS.getCode());
        responseData.setMessage(ErrorCode.SUCCESS.getCodeDesc());
        responseData.setData(this.consumer.getContractContext(groupId));
        return responseData;
    }
}
