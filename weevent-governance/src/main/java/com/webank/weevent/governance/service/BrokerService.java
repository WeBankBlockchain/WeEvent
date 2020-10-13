package com.webank.weevent.governance.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.webank.weevent.client.JsonHelper;
import com.webank.weevent.governance.common.ConstantProperties;
import com.webank.weevent.governance.common.ErrorCode;
import com.webank.weevent.governance.common.GovernanceException;
import com.webank.weevent.governance.common.GovernanceResult;
import com.webank.weevent.governance.entity.BrokerEntity;
import com.webank.weevent.governance.entity.PermissionEntity;
import com.webank.weevent.governance.entity.RuleDatabaseEntity;
import com.webank.weevent.governance.entity.RuleEngineEntity;
import com.webank.weevent.governance.enums.IsCreatorEnum;
import com.webank.weevent.governance.enums.IsDeleteEnum;
import com.webank.weevent.governance.mapper.BrokerMapper;
import com.webank.weevent.governance.repository.BrokerRepository;
import com.webank.weevent.governance.repository.PermissionRepository;
import com.webank.weevent.governance.repository.RuleDatabaseRepository;
import com.webank.weevent.governance.repository.RuleEngineRepository;
import com.webank.weevent.governance.repository.TopicHistoricalRepository;
import com.webank.weevent.governance.repository.TopicRepository;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * BrokerService
 *
 * @since 2019/04/28
 */
@Service
@Slf4j
public class BrokerService {


    @Autowired
    private BrokerRepository brokerRepository;

    @Autowired
    private BrokerMapper brokerMapper;


    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private TopicHistoricalRepository topicHistoricalRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private TopicHistoricalService topicHistoricalService;

    @Autowired
    private RuleDatabaseRepository ruleDatabaseRepository;

    @Autowired
    private RuleEngineService ruleEngineService;

    @Autowired
    private RuleEngineRepository ruleEngineRepository;

    @Autowired
    private CommonService commonService;


    private final static String HTTP_GET_SUCCESS_CODE = "0";



    public List<BrokerEntity> getBrokers(HttpServletRequest request, String accountId) {
        List<BrokerEntity> brokerEntityList = brokerMapper.getBrokers(Integer.parseInt(accountId));
        //Set the identity of the creation and authorization
        brokerEntityList.forEach(brokerEntity -> {
            List<RuleEngineEntity> ruleEngineEntityList = ruleEngineRepository.findAllByBrokerIdAndSystemTagAndDeleteAt(brokerEntity.getId(), true, IsDeleteEnum.NOT_DELETED.getCode());
            List<Integer> integerList = ruleEngineEntityList.stream().map(RuleEngineEntity::getId).collect(Collectors.toList());
            brokerEntity.setRuleIdList(integerList);
            if (accountId.equals(brokerEntity.getUserId().toString())) {
                brokerEntity.setIsCreator(IsCreatorEnum.CREATOR.getCode());
            } else {
                brokerEntity.setIsCreator(IsCreatorEnum.AUTHORIZED.getCode());
            }
        });
        return brokerEntityList;
    }

    public BrokerEntity getBroker(Integer id) {
        return brokerRepository.findByIdAndDeleteAt(id, IsDeleteEnum.NOT_DELETED.getCode());
    }

    @Transactional(rollbackFor = Throwable.class)
    public GovernanceResult<Integer> addBroker(BrokerEntity brokerEntity, HttpServletRequest request, HttpServletResponse response)
            throws GovernanceException {
        //check  broker serverUrl
        ErrorCode errorCode = checkServerByBrokerEntity(brokerEntity, request);
        if (errorCode.getCode() != ErrorCode.SUCCESS.getCode()) {
            throw new GovernanceException(errorCode);
        }
        //checkBrokerUrlRepeat
        boolean repeat = checkBrokerUrlRepeat(brokerEntity);
        if (!repeat) {
            return new GovernanceResult<>(ErrorCode.BROKER_REPEAT);
        }

        try {
            brokerRepository.save(brokerEntity);
            //create permissionEntityList
            List<PermissionEntity> permissionEntityList = createPerMissionList(brokerEntity);
            if (permissionEntityList.size() > 0) {
                permissionRepository.saveAll(permissionEntityList);
            }
            // Create a table based on the brokerId and groupId, start a rule engine
            //determine if the processor's service is available
            boolean exist = ruleEngineService.checkProcessorExist(request);
            log.info("exist:{}", exist);
            if (exist) {
                log.info("processor exist");
                topicHistoricalService.createRule(request, response, brokerEntity);
            }
            return GovernanceResult.ok(brokerEntity.getId());
        } catch (Exception e) {
            log.error("add broker fail", e);
            throw new GovernanceException("add broker fail" + e.getMessage());
        }
    }

    private boolean checkBrokerUrlRepeat(BrokerEntity brokerEntity) {
        BrokerEntity broker = new BrokerEntity();
        broker.setBrokerUrl(brokerEntity.getBrokerUrl().trim());
        Example<BrokerEntity> example = Example.of(broker);
        List<BrokerEntity> brokerEntities = brokerRepository.findAll(example);

        if (CollectionUtils.isEmpty(brokerEntities)) {
            return true;
        }
        if (brokerEntities.size() > 1) {
            return false;
        }
        return brokerEntity.getId() != null && brokerEntities.get(0).getId().intValue() == brokerEntity.getId().intValue();
    }

    private List<PermissionEntity> createPerMissionList(BrokerEntity brokerEntity) {
        log.info("brokerId: {}", brokerEntity.getId());
        List<PermissionEntity> permissionEntityList = new ArrayList<>();
        List<Integer> userIdList = brokerEntity.getUserIdList();
        if (userIdList == null) {
            return permissionEntityList;
        }
        userIdList.forEach(userId -> {
            PermissionEntity permissionEntity = new PermissionEntity();
            permissionEntityList.add(permissionEntity);
            permissionEntity.setUserId(userId);
            permissionEntity.setBrokerId(brokerEntity.getId());
        });
        return permissionEntityList;
    }

    @Transactional(rollbackFor = Throwable.class)
    public GovernanceResult<Boolean> deleteBroker(BrokerEntity brokerEntity, HttpServletRequest request) throws GovernanceException {
        try {

            deleteOldData(brokerEntity, request);
            brokerRepository.deleteById(brokerEntity.getId(), new Date().getTime());
        } catch (Exception e) {
            log.info("delete broker fail", e);
            throw new GovernanceException("delete broker fail" + e.getMessage());
        }

        return GovernanceResult.ok(true);
    }

    @Transactional(rollbackFor = Throwable.class)
    public GovernanceResult<Object> updateBroker(BrokerEntity brokerEntity, HttpServletRequest request, HttpServletResponse response)
            throws GovernanceException {
        //check  broker  serverUrl
        ErrorCode errorCode = checkServerByBrokerEntity(brokerEntity, request);
        if (errorCode.getCode() != ErrorCode.SUCCESS.getCode()) {
            throw new GovernanceException(errorCode);
        }
        //checkBrokerUrlRepeat
        boolean repeat = checkBrokerUrlRepeat(brokerEntity);
        if (!repeat) {
            return new GovernanceResult<>(ErrorCode.BROKER_REPEAT);
        }
        brokerEntity.setLastUpdate(new Date());
        /**
         * Determine if it is the original URL.
         * If it is, do not perform rules.
         * If it is not delete the original rule, create and start a new rule
         */
        modifyRule(brokerEntity, request, response);
        //save broker
        brokerRepository.save(brokerEntity);
        //delete old permission
        permissionRepository.deletePermissionByBrokerId(brokerEntity.getId());
        //create new permission
        List<PermissionEntity> perMissionList = createPerMissionList(brokerEntity);
        if (perMissionList.size() > 0) {
            permissionRepository.saveAll(perMissionList);
        }
        return GovernanceResult.ok(true);
    }

    private void modifyRule(BrokerEntity brokerEntity, HttpServletRequest request, HttpServletResponse response) throws GovernanceException {
        BrokerEntity oldBroker = brokerRepository.findByIdAndDeleteAt(brokerEntity.getId(), IsDeleteEnum.NOT_DELETED.getCode());
        if (brokerEntity.getBrokerUrl().equals(oldBroker.getBrokerUrl())) {
            return;
        }
        //delete old rule
        deleteOldData(brokerEntity, request);
        //add new rule
        topicHistoricalService.createRule(request, response, brokerEntity);

    }

    private ErrorCode checkServerByBrokerEntity(BrokerEntity brokerEntity, HttpServletRequest request) throws GovernanceException {
        if (StringUtils.isBlank(brokerEntity.getBrokerUrl())) {
            return ErrorCode.ILLEGAL_INPUT;
        }

        //checkServerUrl
        return check(brokerEntity, request);
    }

    public ErrorCode checkServerByUrl(BrokerEntity brokerEntity, HttpServletRequest request) throws GovernanceException {
        //check broker  serverUrl
        if (StringUtils.isBlank(brokerEntity.getBrokerUrl())) {
            return ErrorCode.ILLEGAL_INPUT;
        }

        return check(brokerEntity, request);
    }

    private ErrorCode check(BrokerEntity brokerEntity, HttpServletRequest request) {
        if (!StringUtils.isBlank(brokerEntity.getBrokerUrl())) {
            String brokerServerUrl = brokerEntity.getBrokerUrl();
            log.info("check Broker server, url:{}", brokerServerUrl);
            try {
                checkUrl(brokerServerUrl, ConstantProperties.BROKER_LIST_URL, request);
            } catch (GovernanceException e) {
                log.error("check Broker server failed. e", e);
                return ErrorCode.BROKER_CONNECT_ERROR;
            }
        }
        return ErrorCode.SUCCESS;
    }

    private void checkUrl(String url, String afterUrl, HttpServletRequest request) throws GovernanceException {
        // get httpclient
        String headUrl = url;
        CloseableHttpClient client = commonService.generateHttpClient();
        // get one of broker urls
        headUrl = headUrl + afterUrl;
        HttpGet get = commonService.getMethod(headUrl, request);
        Map jsonObject;

        try {
            CloseableHttpResponse response = client.execute(get);
            String responseResult = EntityUtils.toString(response.getEntity());
            jsonObject = JsonHelper.json2Object(responseResult, Map.class);
        } catch (Exception e) {
            log.error("url {}, connect fail,error:{}", headUrl, e.getMessage());
            throw new GovernanceException("url:{}" + headUrl + " connect fail", e);
        }

        if (!HTTP_GET_SUCCESS_CODE.equals(String.valueOf(jsonObject.get("code")))) {
            log.error("url {}, connect fail.", headUrl);
            throw new GovernanceException("url " + headUrl + " connect fail");
        }
    }

    private void deleteOldData(BrokerEntity brokerEntity, HttpServletRequest request) throws GovernanceException {
        //delete rule
        boolean exist = ruleEngineService.checkProcessorExist(request);
        log.info("exist:{}", exist);
        if (exist) {
            deleteRule(brokerEntity, request);
        }
        topicRepository.deleteByBrokerId(brokerEntity.getId(), new Date().getTime());
        permissionRepository.deletePermissionByBrokerId(brokerEntity.getId());
        topicHistoricalRepository.deleteTopicHistoricalByBrokerId(brokerEntity.getId());

        List<RuleEngineEntity> ruleEngineEntityList = ruleEngineRepository.findAllByBrokerIdAndDeleteAt(brokerEntity.getId(), IsDeleteEnum.NOT_DELETED.getCode());
        ruleEngineEntityList.forEach(it -> ruleEngineRepository.deleteRuleEngine(it.getId(), new Date().getTime()));

        List<RuleDatabaseEntity> databaseEntityList = ruleDatabaseRepository.findAllByBrokerIdAndSystemTag(brokerEntity.getId(), true);
        ruleDatabaseRepository.deleteAll(databaseEntityList);
    }

    private void deleteRule(BrokerEntity brokerEntity, HttpServletRequest request) throws GovernanceException {
        try {
            List<RuleEngineEntity> ruleEngines = ruleEngineRepository.findAllByBrokerIdAndDeleteAt(brokerEntity.getId(), IsDeleteEnum.NOT_DELETED.getCode());
            if (CollectionUtils.isNotEmpty(ruleEngines)) {
                for (RuleEngineEntity ruleEngine : ruleEngines) {
                    ruleEngineService.deleteProcessRule(request, ruleEngine);
                }
            }
        } catch (Exception e) {
            log.error("delete rule fail", e);
            throw new GovernanceException("delete rule fail", e);
        }
    }

}
