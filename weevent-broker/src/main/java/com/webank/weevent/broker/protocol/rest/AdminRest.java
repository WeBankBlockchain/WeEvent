package com.webank.weevent.broker.protocol.rest;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.webank.weevent.broker.config.BuildInfo;
import com.webank.weevent.client.BaseResponse;
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
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
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
    private String appName;
    private IConsumer consumer;
    private BuildInfo buildInfo;
    private DiscoveryClient discoveryClient;

    @Autowired
    public void setEnvironment(Environment environment) {
        this.appName = environment.getProperty("spring.application.name");
    }

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
    public BaseResponse<List<String>> listGroup() throws BrokerException {
        return BaseResponse.buildSuccess(this.consumer.listGroupId());
    }

    @RequestMapping(path = "/listNodes")
    public BaseResponse<List<String>> listNodes() {
        log.info("query node list");

        List<String> nodesInfo = new ArrayList<>();
        List<ServiceInstance> instances = this.discoveryClient.getInstances(this.appName);
        for (ServiceInstance serviceInstance : instances) {
            nodesInfo.add(serviceInstance.getInstanceId());
        }

        return BaseResponse.buildSuccess(nodesInfo);
    }

    @RequestMapping(path = "/listSubscription")
    public BaseResponse<Map<String, List<SubscriptionInfo>>> listSubscription(@RequestParam(name = "nodeInstances") String nodeInstances,
                                                                              @RequestParam(name = "groupId", required = false) String groupId) {
        log.info("groupId:{}, nodeInstances:{}", groupId, nodeInstances);
        if (StringUtils.isBlank(nodeInstances)) {
            log.error("nodeInstances is empty.");
            return BaseResponse.buildFail(ErrorCode.CGI_INVALID_INPUT);
        }

        List<ServiceInstance> instances = this.discoveryClient.getInstances(this.appName);

        Map<String, List<SubscriptionInfo>> subscriptions = new HashMap<>();
        String[] instanceList = nodeInstances.split(",");
        for (String instanceId : instanceList) {
            Optional<ServiceInstance> instance = instances.stream().filter(item -> item.getInstanceId().equals(instanceId)).findFirst();
            if (instance.isPresent()) {
                String url = String.format("%s/%s/admin/innerListSubscription", instance.get().getUri(), this.appName);
                if (!StringUtils.isBlank(groupId)) {
                    url += "?groupId=" + groupId;
                }
                log.info("url: {}", url);

                SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
                RestTemplate rest = new RestTemplate(requestFactory);
                ResponseEntity<BaseResponse<Map<String, SubscriptionInfo>>> rsp = rest.exchange(url,
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<BaseResponse<Map<String, SubscriptionInfo>>>() {
                        });
                if (rsp.getStatusCode() == HttpStatus.OK && rsp.getBody() != null) {
                    log.debug("innerListSubscription: {}", rsp);
                    subscriptions.put(instanceId, new ArrayList<>(rsp.getBody().getData().values()));
                    continue;
                }
            }
            subscriptions.put(instanceId, Collections.emptyList());
        }

        return BaseResponse.buildSuccess(subscriptions);
    }

    @RequestMapping(path = "/innerListSubscription")
    public BaseResponse<Map<String, SubscriptionInfo>> innerListSubscription(@RequestParam(name = "groupId", required = false) String groupId) throws BrokerException {
        log.info("groupId: {}", groupId);

        return BaseResponse.buildSuccess(this.consumer.listSubscription(groupId));
    }

    @RequestMapping(path = "/getVersion")
    public BaseResponse<BuildInfo> getVersion() {
        return BaseResponse.buildSuccess(this.buildInfo);
    }

    /**
     * get general
     */
    @RequestMapping(path = "/group/general")
    public BaseResponse<GroupGeneral> getGroupGeneral(@RequestParam(value = "groupId", required = false) String groupId) throws BrokerException {
        log.info("groupId: {}", groupId);

        GroupGeneral groupGeneral = this.consumer.getGroupGeneral(groupId);

        return BaseResponse.buildSuccess(groupGeneral);
    }


    /**
     * query transaction list.
     */
    @RequestMapping(path = "/transaction/transList")
    public BaseResponse<ListPage<TbTransHash>> queryTransList(@RequestParam(value = "groupId", required = false) String groupId,
                                                              @RequestParam("pageNumber") Integer pageNumber,
                                                              @RequestParam("pageSize") Integer pageSize,
                                                              @RequestParam(value = "transactionHash", required = false) String transHash,
                                                              @RequestParam(value = "blockNumber", required = false) BigInteger blockNumber) throws BrokerException {
        log.info("groupId:{} pageNumber:{} pageSize:{} pkHash:{} blockNumber:{}", groupId, pageNumber, pageSize, transHash, blockNumber);

        QueryEntity queryEntity = new QueryEntity(groupId, pageNumber, pageSize, transHash, blockNumber);
        ListPage<TbTransHash> tbTransHashes = this.consumer.queryTransList(queryEntity);

        return BaseResponse.buildSuccess(tbTransHashes);
    }


    /**
     * query block list.
     */
    @RequestMapping(path = "/block/blockList")
    public BaseResponse<ListPage<TbBlock>> queryBlockList(@RequestParam(value = "groupId", required = false) String groupId,
                                                          @RequestParam("pageNumber") Integer pageNumber,
                                                          @RequestParam("pageSize") Integer pageSize,
                                                          @RequestParam(value = "pkHash", required = false) String pkHash,
                                                          @RequestParam(value = "blockNumber", required = false) BigInteger blockNumber) throws BrokerException {
        log.info("groupId:{} pageNumber:{} pageSize:{} pkHash:{} blockNumber:{}", groupId, pageNumber, pageSize, pkHash, blockNumber);

        QueryEntity queryEntity = new QueryEntity(groupId, pageNumber, pageSize, pkHash, blockNumber);
        ListPage<TbBlock> tbBlocks = this.consumer.queryBlockList(queryEntity);

        return BaseResponse.buildSuccess(tbBlocks);
    }

    /**
     * query node info list.
     */
    @RequestMapping(path = "/node/nodeList")
    public BaseResponse<ListPage<TbNode>> queryNodeList(@RequestParam(value = "groupId", required = false) String groupId,
                                                        @RequestParam("pageNumber") Integer pageNumber,
                                                        @RequestParam("pageSize") Integer pageSize,
                                                        @RequestParam(value = "nodeName", required = false) String nodeName) throws BrokerException {
        log.info("groupId:{}  pageNumber:{} pageSize:{} nodeName:{}", groupId, pageNumber, pageSize, nodeName);

        QueryEntity queryEntity = new QueryEntity(groupId, pageNumber, pageSize, null, null);
        queryEntity.setNodeName(nodeName);
        ListPage<TbNode> tbNodeListPage = this.consumer.queryNodeList(queryEntity);
        tbNodeListPage.setPageIndex(pageNumber);
        tbNodeListPage.setPageSize(pageSize);

        return BaseResponse.buildSuccess(tbNodeListPage);
    }

    /**
     * query ContractContext.
     */
    @RequestMapping(path = "/getContractContext")
    public BaseResponse<ContractContext> getContractContext(@RequestParam(value = "groupId", required = false) String groupId) throws BrokerException {
        log.info("groupId: {} ", groupId);

        return BaseResponse.buildSuccess(this.consumer.getContractContext(groupId));
    }
}
