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
import com.webank.weevent.broker.plugin.IProducer;
import com.webank.weevent.protocol.rest.entity.GroupGeneral;
import com.webank.weevent.protocol.rest.entity.QueryEntity;
import com.webank.weevent.protocol.rest.entity.TbBlock;
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
import org.springframework.web.bind.annotation.PathVariable;
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
    private IProducer producer;
    private BuildInfo buildInfo;

    @Autowired
    public void setConsumer(IConsumer consumer) {
        this.consumer = consumer;
    }

    @Autowired
    public void setConsumer(BuildInfo buildInfo) {
        this.buildInfo = buildInfo;
    }

    @Autowired
    public void setProducer(IProducer producer) {
        this.producer = producer;
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
                log.error(e.toString());
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
        responseData.setResult(this.buildInfo);
        return responseData;
    }

    /**
     * get general
     */
    @RequestMapping(path = "/group/general/{groupId}")
    public ResponseData getGroupGeneral(@PathVariable("groupId") Integer groupId) throws BrokerException {
        ResponseData responseData = new ResponseData();
        Instant startTime = Instant.now();
        log.info("start getGroupGeneral startTime:{} groupId:{}", startTime.toEpochMilli(),
                groupId);

        GroupGeneral groupGeneral = this.producer.getGroupGeneral(String.valueOf(groupId));
        responseData.setCode(ErrorCode.SUCCESS.getCode());
        responseData.setMessage(ErrorCode.SUCCESS.getCodeDesc());
        responseData.setResult(groupGeneral);
        return responseData;
    }


    /**
     * query transaction list.
     */
    @RequestMapping(path = "/transaction/transList/{groupId}/{pageNumber}/{pageSize}")
    public ResponseData queryTransList(@PathVariable("groupId") Integer groupId,
                                       @PathVariable("pageNumber") Integer pageNumber,
                                       @PathVariable("pageSize") Integer pageSize,
                                       @RequestParam(value = "transactionHash", required = false) String transHash,
                                       @RequestParam(value = "blockNumber", required = false) BigInteger blockNumber)
            throws BrokerException {
        Instant startTime = Instant.now();
        log.info(
                "start queryTransList startTime:{} groupId:{} pageNumber:{} pageSize:{} "
                        + "pkHash:{} blockNumber:{}",
                startTime.toEpochMilli(), groupId,
                pageNumber, pageSize, transHash, blockNumber);

        ResponseData responseData = new ResponseData();
        QueryEntity queryEntity = new QueryEntity(groupId, pageNumber, pageSize, transHash, blockNumber);

        List<TbTransHash> tbTransHashes = this.producer.queryTransList(queryEntity);
        responseData.setCode(ErrorCode.SUCCESS.getCode());
        responseData.setMessage(ErrorCode.SUCCESS.getCodeDesc());
        responseData.setResult(tbTransHashes);
        return responseData;
    }


    /**
     * query block list.
     */
    @RequestMapping(path = "/block/blockList/{groupId}/{pageNumber}/{pageSize}")
    public ResponseData queryBlockList(@PathVariable("groupId") Integer groupId,
                                       @PathVariable("pageNumber") Integer pageNumber,
                                       @PathVariable("pageSize") Integer pageSize,
                                       @RequestParam(value = "pkHash", required = false) String pkHash,
                                       @RequestParam(value = "blockNumber", required = false) BigInteger blockNumber)
            throws BrokerException {
        Instant startTime = Instant.now();
        log.info(
                "start queryBlockList startTime:{} groupId:{} pageNumber:{} pageSize:{} "
                        + "pkHash:{} blockNumber:{}",
                startTime.toEpochMilli(), groupId,
                pageNumber, pageSize, pkHash, blockNumber);

        ResponseData responseData = new ResponseData();
        QueryEntity queryEntity = new QueryEntity(groupId, pageNumber, pageSize, pkHash, blockNumber);

        List<TbBlock> tbBlocks = this.producer.queryBlockList(queryEntity);
        responseData.setCode(ErrorCode.SUCCESS.getCode());
        responseData.setMessage(ErrorCode.SUCCESS.getCodeDesc());
        responseData.setResult(tbBlocks.toArray());
        return responseData;
    }

    /**
     * qurey node info list.
     */
    @RequestMapping(path = "/node/nodeList/{groupId}/{pageNumber}/{pageSize}")
    public ResponseData queryNodeList(@PathVariable("groupId") Integer groupId,
                                      @PathVariable("pageNumber") Integer pageNumber,
                                      @PathVariable("pageSize") Integer pageSize,
                                      @RequestParam(value = "nodeName", required = false) String nodeName)
            throws BrokerException {
        ResponseData responseData = new ResponseData();
        responseData.setCode(ErrorCode.SUCCESS.getCode());
        responseData.setMessage(ErrorCode.SUCCESS.getCodeDesc());
        return responseData;
    }

    /**
     * query the number of transactions in the last week
     */
    @GetMapping("/group/transDaily/{groupId}")
    public ResponseData getTransDaily(@PathVariable("groupId") Integer groupId)
            throws BrokerException {
        ResponseData responseData = new ResponseData();
        responseData.setCode(ErrorCode.SUCCESS.getCode());
        responseData.setMessage(ErrorCode.SUCCESS.getCodeDesc());
        return responseData;
    }

}
