package com.webank.weevent.protocol.rest;

import java.math.BigInteger;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.webank.weevent.BrokerApplication;
import com.webank.weevent.broker.config.BuildInfo;
import com.webank.weevent.broker.fisco.util.SystemInfoUtils;
import com.webank.weevent.broker.plugin.IConsumer;
import com.webank.weevent.protocol.rest.entity.GroupGeneral;
import com.webank.weevent.protocol.rest.entity.QueryEntity;
import com.webank.weevent.protocol.rest.entity.TbBlock;
import com.webank.weevent.protocol.rest.entity.TbNode;
import com.webank.weevent.protocol.rest.entity.TbTransHash;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.ErrorCode;
import com.webank.weevent.sdk.WeEvent;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.bind.annotation.GetMapping;
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

    @RequestMapping(path = "/listSubscription")
    public Map<String, Object> listSubscription(@RequestParam(name = "groupId", required = false) String groupIdStr) throws BrokerException {
        String groupId = groupIdStr;
        if (StringUtils.isBlank(groupId)) {
            groupId = WeEvent.DEFAULT_GROUP_ID;
        }
        Map<String, Object> nodesInfo = new HashMap<>();
        if (this.masterJob.getClient() == null) {
            nodesInfo.put(SystemInfoUtils.getCurrentIp() + ":" + SystemInfoUtils.getCurrentPort(),
                    this.consumer.listSubscription(groupId));
        } else {
            try {
                List<String> ipList = this.masterJob.getClient().getChildren().forPath(BrokerApplication.weEventConfig.getZookeeperPath() + "/nodes");
                log.info("zookeeper ip List:{}", ipList);
                for (String nodeIP : ipList) {
                    byte[] ip = this.masterJob.getZookeeperNode(BrokerApplication.weEventConfig.getZookeeperPath() + "/nodes" + "/" + nodeIP);
                    SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
                    RestTemplate rest = new RestTemplate(requestFactory);
                    String url = new StringBuffer("http://").append(new String(ip)).append("/weevent/admin/innerListSubscription")
                            .append("?groupId=").append(groupId).toString();
                    log.info("url:{}", url);

                    ResponseEntity<String> rsp = rest.getForEntity(url, String.class);
                    log.debug("innerListSubscription:{}", JSON.parse(rsp.getBody()));
                    nodesInfo.put(new String(ip), JSON.parse(rsp.getBody()));
                }
            } catch (Exception e) {
                log.error("find subscriptionList fail", e);
                throw new BrokerException("find subscriptionList fail", e);
            }
        }


        return nodesInfo;
    }

    @RequestMapping(path = "/innerListSubscription")
    public Map<String, Object> innerListSubscription(@RequestParam(name = "groupId") String groupId) throws BrokerException {
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
    public ResponseData<GroupGeneral> getGroupGeneral(@RequestParam("groupId") Integer groupId) throws BrokerException {
        ResponseData<GroupGeneral> responseData = new ResponseData<>();
        Instant startTime = Instant.now();
        log.info("start getGroupGeneral startTime:{} groupId:{}", startTime.toEpochMilli(),
                groupId);

        GroupGeneral groupGeneral = this.consumer.getGroupGeneral(String.valueOf(groupId));
        responseData.setCode(ErrorCode.SUCCESS.getCode());
        responseData.setMessage(ErrorCode.SUCCESS.getCodeDesc());
        responseData.setData(groupGeneral);
        return responseData;
    }


    /**
     * query transaction list.
     */
    @RequestMapping(path = "/transaction/transList")
    public ResponseData<List<TbTransHash>> queryTransList(@RequestParam("groupId") Integer groupId,
                                                          @RequestParam("pageNumber") Integer pageNumber,
                                                          @RequestParam("pageSize") Integer pageSize,
                                                          @RequestParam(value = "transactionHash", required = false) String transHash,
                                                          @RequestParam(value = "blockNumber", required = false) BigInteger blockNumber)
            throws BrokerException {
        Instant startTime = Instant.now();
        log.info(
                "start queryTransList startTime:{} groupId:{} pageNumber:{} pageSize:{} "
                        + "pkHash:{} blockNumber:{}",
                startTime.toEpochMilli(), groupId,
                pageNumber, pageSize, transHash, blockNumber);

        ResponseData<List<TbTransHash>> responseData = new ResponseData<>();
        QueryEntity queryEntity = new QueryEntity(groupId.toString(), pageNumber, pageSize, transHash, blockNumber);

        List<TbTransHash> tbTransHashes = this.consumer.queryTransList(queryEntity);
        responseData.setCode(ErrorCode.SUCCESS.getCode());
        responseData.setMessage(ErrorCode.SUCCESS.getCodeDesc());
        responseData.setData(tbTransHashes);
        responseData.setTotalCount(tbTransHashes == null ? 0 : tbTransHashes.size());
        return responseData;
    }


    /**
     * query block list.
     */
    @RequestMapping(path = "/block/blockList")
    public ResponseData<List<TbBlock>> queryBlockList(@RequestParam("groupId") Integer groupId,
                                                      @RequestParam("pageNumber") Integer pageNumber,
                                                      @RequestParam("pageSize") Integer pageSize,
                                                      @RequestParam(value = "pkHash", required = false) String pkHash,
                                                      @RequestParam(value = "blockNumber", required = false) BigInteger blockNumber)
            throws BrokerException {
        Instant startTime = Instant.now();
        log.info(
                "start queryBlockList startTime:{} groupId:{} pageNumber:{} pageSize:{} "
                        + "pkHash:{} blockNumber:{}",
                startTime.toEpochMilli(), groupId,
                pageNumber, pageSize, pkHash, blockNumber);

        ResponseData<List<TbBlock>> responseData = new ResponseData<>();
        QueryEntity queryEntity = new QueryEntity(groupId.toString(), pageNumber, pageSize, pkHash, blockNumber);

        List<TbBlock> tbBlocks = this.consumer.queryBlockList(queryEntity);
        responseData.setCode(ErrorCode.SUCCESS.getCode());
        responseData.setMessage(ErrorCode.SUCCESS.getCodeDesc());
        responseData.setData(tbBlocks.toArray());
        responseData.setTotalCount(tbBlocks.size());
        return responseData;
    }

    /**
     * qurey node info list.
     */
    @RequestMapping(path = "/node/nodeList")
    public ResponseData<List<TbNode>> queryNodeList(@RequestParam("groupId") Integer groupId,
                                                    @RequestParam("pageNumber") Integer pageNumber,
                                                    @RequestParam("pageSize") Integer pageSize,
                                                    @RequestParam(value = "nodeName", required = false) String nodeName)
            throws BrokerException {
        Instant startTime = Instant.now();
        log.info(
                "start queryNodeList startTime:{} groupId:{}  pageNumber:{} pageSize:{} nodeName:{}",
                startTime.toEpochMilli(), groupId, pageNumber,
                pageSize, nodeName);
        QueryEntity queryEntity = new QueryEntity(groupId.toString(), pageNumber, pageSize, null, null);
        queryEntity.setNodeName(nodeName);
        ResponseData<List<TbNode>> responseData = new ResponseData<>();
        responseData.setCode(ErrorCode.SUCCESS.getCode());
        responseData.setMessage(ErrorCode.SUCCESS.getCodeDesc());
        List<TbNode> tbNodeList = this.consumer.queryNodeList(queryEntity);
        responseData.setTotalCount(tbNodeList == null ? 0 : tbNodeList.size());
        responseData.setData(tbNodeList);
        return responseData;
    }

    /**
     * query the number of transactions in the last week
     */
    @GetMapping("/group/transDaily")
    public ResponseData getTransDaily(@RequestParam("groupId") Integer groupId)
            throws BrokerException {
        ResponseData responseData = new ResponseData();
        responseData.setCode(ErrorCode.SUCCESS.getCode());
        responseData.setMessage(ErrorCode.SUCCESS.getCodeDesc());
        return responseData;
    }

}