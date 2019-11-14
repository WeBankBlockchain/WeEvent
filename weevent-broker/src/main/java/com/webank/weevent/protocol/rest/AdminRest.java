package com.webank.weevent.protocol.rest;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.webank.weevent.BrokerApplication;
import com.webank.weevent.broker.config.BuildInfo;
import com.webank.weevent.broker.fisco.dto.ListPage;
import com.webank.weevent.broker.fisco.util.SystemInfoUtils;
import com.webank.weevent.broker.plugin.IConsumer;
import com.webank.weevent.protocol.rest.entity.GroupGeneral;
import com.webank.weevent.protocol.rest.entity.QueryEntity;
import com.webank.weevent.protocol.rest.entity.TbBlock;
import com.webank.weevent.protocol.rest.entity.TbNode;
import com.webank.weevent.protocol.rest.entity.TbTransHash;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.ErrorCode;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
public class AdminRest extends RestHA {
    private IConsumer consumer;
    private BuildInfo buildInfo;

    @Autowired
    public void setConsumer(IConsumer consumer) {
        this.consumer = consumer;
    }

    @Autowired
    public void setBuildInfo(BuildInfo buildInfo) {
        this.buildInfo = buildInfo;
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
        if (this.masterJob.getClient() == null) {
            nodesInfo.add(SystemInfoUtils.getCurrentIp() + ":" + SystemInfoUtils.getCurrentPort());
        } else {
            try {
                List<String> ipList = this.masterJob.getClient().getChildren().forPath(BrokerApplication.weEventConfig.getZookeeperPath() + "/nodes");
                log.info("zookeeper ip List:{}", ipList);
                for (String nodeIP : ipList) {
                    byte[] ip = this.masterJob.getZookeeperNode(BrokerApplication.weEventConfig.getZookeeperPath() + "/nodes" + "/" + nodeIP);
                    nodesInfo.add(new String(ip));
                }
            } catch (Exception e) {
                log.error("find listNodes fail", e);
                throw new BrokerException("find listNodes fail", e);
            }
        }
        responseData.setErrorCode(ErrorCode.SUCCESS);
        responseData.setData(nodesInfo);
        return responseData;
    }

    @RequestMapping(path = "/listSubscription")
    public ResponseData<Map<String, Object>> listSubscription(@RequestParam(name = "nodeIp") String nodeIp,
                                                              @RequestParam(name = "groupId", required = false) String groupId) throws BrokerException {
        log.info("groupId:{}, nodeIp:{}", groupId, nodeIp);

        ResponseData<Map<String, Object>> responseData = new ResponseData<>();
        if (StringUtils.isBlank(nodeIp)) {
            log.error("node ipList is null.");
            throw new BrokerException("node ipList is null.");
        }

        Map<String, Object> nodesInfo = new HashMap<>();
        if (this.masterJob.getClient() == null) {
            nodesInfo.put(SystemInfoUtils.getCurrentIp() + ":" + SystemInfoUtils.getCurrentPort(),
                    this.consumer.listSubscription(groupId));
        } else {
            try {
                log.info("zookeeper ip List:{}", nodeIp);
                String[] ipList = nodeIp.split(",");
                for (String ipStr : ipList) {
                    if (!StringUtils.isBlank(ipStr)) {
                        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
                        RestTemplate rest = new RestTemplate(requestFactory);
                        String url = new StringBuffer("http://").append(ipStr).append("/weevent/admin/innerListSubscription")
                                .append("?groupId=").append(groupId).toString();
                        log.info("url:{}", url);

                        ResponseEntity<String> rsp = rest.getForEntity(url, String.class);
                        log.debug("innerListSubscription:{}", JSON.parse(rsp.getBody()));
                        nodesInfo.put(nodeIp, JSON.parse(rsp.getBody()));
                    }
                }
            } catch (Exception e) {
                log.error("find subscriptionList fail", e);
                throw new BrokerException("find subscriptionList fail", e);
            }
        }

        responseData.setErrorCode(ErrorCode.SUCCESS);
        responseData.setData(nodesInfo);
        return responseData;
    }

    @RequestMapping(path = "/innerListSubscription")
    public Map<String, Object> innerListSubscription(@RequestParam(name = "groupId") String groupId) throws BrokerException {
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
     * qurey node info list.
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
}
