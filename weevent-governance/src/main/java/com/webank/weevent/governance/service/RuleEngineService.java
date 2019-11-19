package com.webank.weevent.governance.service;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webank.weevent.governance.code.ErrorCode;
import com.webank.weevent.governance.entity.BrokerEntity;
import com.webank.weevent.governance.entity.RuleDatabaseEntity;
import com.webank.weevent.governance.entity.RuleEngineConditionEntity;
import com.webank.weevent.governance.entity.RuleEngineEntity;
import com.webank.weevent.governance.enums.ConditionTypeEnum;
import com.webank.weevent.governance.enums.PayloadEnum;
import com.webank.weevent.governance.enums.StatusEnum;
import com.webank.weevent.governance.exception.GovernanceException;
import com.webank.weevent.governance.mapper.BrokerMapper;
import com.webank.weevent.governance.mapper.RuleDatabaseMapper;
import com.webank.weevent.governance.mapper.RuleEngineConditionMapper;
import com.webank.weevent.governance.mapper.RuleEngineMapper;
import com.webank.weevent.governance.properties.ConstantProperties;
import com.webank.weevent.governance.utils.CookiesTools;
import com.webank.weevent.governance.utils.DAGDetectUtil;
import com.webank.weevent.governance.utils.NumberValidationUtils;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.jsoup.helper.StringUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

/**
 * BrokerService
 *
 * @since 2019/04/28
 */
@Service
@Slf4j
public class RuleEngineService {

    @Autowired
    private RuleEngineMapper ruleEngineMapper;

    @Autowired
    private CommonService commonService;

    @Autowired
    private CookiesTools cookiesTools;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private BrokerMapper brokerMapper;

    @Autowired
    private DAGDetectUtil dagDetectUtil;


    @Value("${weevent.processor.url:http://127.0.0.1:7008}")
    private String processorUrl;

    @Autowired
    private RuleEngineConditionMapper ruleEngineConditionMapper;

    @Autowired
    private RuleDatabaseMapper ruleDatabaseMapper;


    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YYYY-MM-dd hh:mm:ss");


    @Autowired
    private BrokerService brokerService;

    private static final int PROCESSOR_SUCCESS_CODE = 0;

    private final String ERROR_MSG = "success";

    @SuppressWarnings("unchecked")
    public List<RuleEngineEntity> getRuleEngines(HttpServletRequest request, RuleEngineEntity ruleEngineEntity) throws GovernanceException {
        try {
            String accountId = cookiesTools.getCookieValueByName(request, ConstantProperties.COOKIE_MGR_ACCOUNT_ID);

            if (accountId == null || !accountId.equals(ruleEngineEntity.getUserId().toString())) {
                throw new GovernanceException(ErrorCode.ACCESS_DENIED);
            }
            ruleEngineEntity.setSystemTag("2");
            int count = ruleEngineMapper.countRuleEngine(ruleEngineEntity);
            ruleEngineEntity.setTotalCount(count);
            List<RuleEngineEntity> ruleEngineEntities = null;
            if (count > 0) {
                int startIndex = (ruleEngineEntity.getPageNumber() - 1) * ruleEngineEntity.getPageSize();
                int endIndex = ruleEngineEntity.getPageNumber() * ruleEngineEntity.getPageSize();
                ruleEngineEntities = ruleEngineMapper.getRuleEnginePage(ruleEngineEntity, startIndex, endIndex);

                for (RuleEngineEntity it : ruleEngineEntities) {
                    it.setCreateDateStr(simpleDateFormat.format(it.getCreateDate()));
                    String payload = it.getPayload();
                    it.setPayloadMap(payload == null ? new HashMap<>() : JSONObject.parseObject(payload, Map.class));
                }
            }
            return ruleEngineEntities;
        } catch (Exception e) {
            log.error("get ruleEngines fail", e);
            throw new GovernanceException("get ruleEngines fail", e);
        }

    }


    @Transactional(rollbackFor = Throwable.class)
    public RuleEngineEntity addRuleEngine(RuleEngineEntity ruleEngineEntity, HttpServletRequest request, HttpServletResponse response)
            throws GovernanceException {
        try {
            String accountId = cookiesTools.getCookieValueByName(request, ConstantProperties.COOKIE_MGR_ACCOUNT_ID);
            if (accountId == null || !accountId.equals(ruleEngineEntity.getUserId().toString())) {
                throw new GovernanceException(ErrorCode.ACCESS_DENIED);
            }
            ruleEngineEntity.setSystemTag("2");
            ruleEngineEntity.setStatus(StatusEnum.NOT_STARTED.getCode());
            String payload = JSONObject.toJSON(ruleEngineEntity.getPayloadMap()).toString();
            ruleEngineEntity.setPayload(payload);
            if (ruleEngineEntity.getPayloadType() == null || ruleEngineEntity.getPayloadType() == 0) {
                ruleEngineEntity.setPayloadType(PayloadEnum.JSON.getCode());
            }
            ruleEngineEntity.setCreateDate(new Date());
            ruleEngineEntity.setLastUpdate(new Date());
            ruleEngineEntity.setErrorMessage(this.ERROR_MSG);

            //check rule
            this.checkRule(ruleEngineEntity);
            //insert ruleEngine
            ruleEngineMapper.addRuleEngine(ruleEngineEntity);
            //insert processor
            this.addProcessRule(request, ruleEngineEntity);
            return ruleEngineEntity;
        } catch (Exception e) {
            log.error("add ruleEngineEntity fail", e);
            throw new GovernanceException("add ruleEngineEntity fail ", e);
        }
    }

    @SuppressWarnings("unchecked")
    private void addProcessRule(HttpServletRequest request, RuleEngineEntity ruleEngineEntity) throws GovernanceException {
        try {
            //insert processor rule
            BrokerEntity broker = brokerMapper.getBroker(ruleEngineEntity.getBrokerId());
            ruleEngineEntity.setBrokerUrl(broker.getBrokerUrl());
            String url = new StringBuffer(this.getProcessorUrl()).append(ConstantProperties.PROCESSOR_INSERT).toString();
            String jsonString = JSONObject.toJSONString(ruleEngineEntity);
            Map map = JSONObject.parseObject(jsonString, Map.class);
            map.put("updatedTime", ruleEngineEntity.getLastUpdate());
            map.put("createdTime", ruleEngineEntity.getCreateDate());

            log.info("add rule begin====map:{}", JSONObject.toJSONString(map));
            CloseableHttpResponse closeResponse = commonService.getCloseResponse(request, url, JSONObject.toJSONString(map));

            //deal process result
            int statusCode = closeResponse.getStatusLine().getStatusCode();
            if (200 != statusCode) {
                log.error(ErrorCode.PROCESS_CONNECT_ERROR.getCodeDesc());
                throw new GovernanceException(ErrorCode.PROCESS_CONNECT_ERROR);
            }
            String mes = EntityUtils.toString(closeResponse.getEntity());
            JSONObject jsonObject = JSONObject.parseObject(mes);
            Integer code = Integer.valueOf(jsonObject.get("errorCode").toString());
            if (PROCESSOR_SUCCESS_CODE != code) {
                String msg = jsonObject.get("errorMsg").toString();
                throw new GovernanceException(msg);
            }
        } catch (Exception e) {
            log.error("processor add ruleEngine fail! {}", e.getMessage());
            throw new GovernanceException("processor add ruleEngine fail ", e);
        }
    }


    @Transactional(rollbackFor = Throwable.class)
    public boolean deleteRuleEngine(RuleEngineEntity ruleEngineEntity, HttpServletRequest request) throws GovernanceException {
        try {
            authCheck(ruleEngineEntity, request);
            List<RuleEngineEntity> ruleEngines = ruleEngineMapper.getRuleEngines(ruleEngineEntity);
            if (CollectionUtils.isEmpty(ruleEngines)) {
                throw new GovernanceException("the data is deleted ");
            }
            RuleEngineEntity engineEntity = ruleEngines.get(0);
            if (engineEntity.getStatus() != StatusEnum.NOT_STARTED.getCode()) {
                throw new GovernanceException("only unstarted data can be deleted");
            }
            ruleEngineEntity.setStatus(StatusEnum.IS_DELETED.getCode());

            //delete processor rule
            this.deleteProcessRule(request, engineEntity);
            //delete RuleEngineCondition
            RuleEngineConditionEntity ruleEngineConditionEntity = new RuleEngineConditionEntity();
            ruleEngineConditionEntity.setRuleId(ruleEngineEntity.getId());
            ruleEngineConditionMapper.deleteRuleEngineCondition(ruleEngineConditionEntity);
            //delete RuleEngine
            return ruleEngineMapper.deleteRuleEngine(ruleEngineEntity);
        } catch (Exception e) {
            log.error("delete ruleEngineEntity fail", e);
            throw new GovernanceException("delete ruleEngineEntity fail ", e);
        }
    }


    public void deleteProcessRule(HttpServletRequest request, RuleEngineEntity engineEntity) throws GovernanceException {
        try {
            BrokerEntity broker = brokerService.getBroker(engineEntity.getBrokerId());
            String deleteUrl = new StringBuffer(this.getProcessorUrl()).append(ConstantProperties.PROCESSOR_DELETE_CEP_RULE).append(ConstantProperties.QUESTION_MARK)
                    .append("id=").append(engineEntity.getId()).toString();
            CloseableHttpResponse closeResponse = commonService.getCloseResponse(request, deleteUrl);

            //deal processor result
            int statusCode = closeResponse.getStatusLine().getStatusCode();
            if (200 != statusCode) {
                log.error(ErrorCode.PROCESS_CONNECT_ERROR.getCodeDesc());
                throw new GovernanceException(ErrorCode.PROCESS_CONNECT_ERROR);
            }
            String mes = EntityUtils.toString(closeResponse.getEntity());
            JSONObject jsonObject = JSONObject.parseObject(mes);
            Integer code = Integer.valueOf(jsonObject.get("errorCode").toString());
            if (PROCESSOR_SUCCESS_CODE != code) {
                String msg = jsonObject.get("errorMsg").toString();
                throw new GovernanceException(msg);
            }
        } catch (Exception e) {
            log.error("processor delete ruleEngine fail ! {}", e.getMessage());
            throw new GovernanceException("processor delete ruleEngine fail ", e);
        }

    }

    @Transactional(rollbackFor = Throwable.class)
    public boolean updateRuleEngine(RuleEngineEntity ruleEngineEntity, HttpServletRequest request, HttpServletResponse response)
            throws GovernanceException {
        try {
            this.authCheck(ruleEngineEntity, request);
            //check rule
            this.checkRule(ruleEngineEntity);
            //set payload
            String payload = JSONObject.toJSON(ruleEngineEntity.getPayloadMap()).toString();
            ruleEngineEntity.setPayload(payload);
            ruleEngineEntity.setLastUpdate(new Date());

            //set selectFiled 、conditionField
            List<RuleEngineConditionEntity> ruleEngineConditionList = ruleEngineEntity.getRuleEngineConditionList();
            String conditionField = this.getConditionField(ruleEngineConditionList);
            log.info("condition:{}", conditionField);

            ruleEngineEntity.setConditionField(conditionField);

            RuleEngineEntity rule = new RuleEngineEntity();
            rule.setId(ruleEngineEntity.getId());
            List<RuleEngineEntity> ruleEngines = ruleEngineMapper.getRuleEngines(rule);
            rule = ruleEngines.get(0);

            // check sql condition
            boolean flag = validationConditions(request, ruleEngineEntity);
            if (!flag) {
                throw new GovernanceException("conditional is illegal");
            }
            flag = verifyInfiniteLoop(ruleEngineEntity);
            if (!flag) {
                throw new GovernanceException("update rule failed, detected DAG loop at topic [" + ruleEngineEntity.getFromDestination() + "]");
            }
            RuleDatabaseEntity ruleDataBase = getRuleDataBase(ruleEngineEntity.getRuleDataBaseId());
            if (ruleDataBase != null) {
                ruleEngineEntity.setDatabaseUrl(ruleDataBase.getDatabaseUrl() + "&tableName=" + ruleDataBase.getTableName());
                log.info("dataBaseUrl:{}", ruleEngineEntity.getDatabaseUrl());
            }
            checkSourceDestinationTopic(ruleEngineEntity);
            ruleEngineEntity.setStatus(rule.getStatus());

            //update process rule
            BrokerEntity broker = brokerMapper.getBroker(rule.getBrokerId());
            ruleEngineEntity.setBrokerUrl(broker.getBrokerUrl());
            if (rule.getStatus() == StatusEnum.NOT_STARTED.getCode()) {
                this.updateProcessRule(request, ruleEngineEntity, rule);
            } else {
                ruleEngineEntity.setGroupId(rule.getGroupId());
                ruleEngineEntity.setSystemTag("2");
                this.startProcessRule(request, ruleEngineEntity);
            }

            //delete old ruleEngineConditionEntity
            RuleEngineConditionEntity ruleEngineConditionEntity = new RuleEngineConditionEntity();
            ruleEngineConditionEntity.setRuleId(ruleEngineEntity.getId());
            ruleEngineConditionMapper.deleteRuleEngineCondition(ruleEngineConditionEntity);

            //check ruleEngineConditionEntity
            if (!CollectionUtils.isEmpty(ruleEngineConditionList)) {
                //insert ruleEngineCondition
                for (RuleEngineConditionEntity engineConditionEntity : ruleEngineConditionList) {
                    engineConditionEntity.setRuleId(ruleEngineEntity.getId());
                    checkSqlCondition(engineConditionEntity);
                    //get sql json
                    engineConditionEntity.setSqlConditionJson(getSqlJson(engineConditionEntity));
                }
                //insert new data
                ruleEngineConditionMapper.batchInsert(ruleEngineConditionList);
            }
            return ruleEngineMapper.updateRuleEngine(ruleEngineEntity);
        } catch (Exception e) {
            log.error("update ruleEngine fail", e);
            throw new GovernanceException("update ruleEngine fail", e);
        }

    }

    private void checkSourceDestinationTopic(RuleEngineEntity ruleEngineEntity) throws GovernanceException {
        ArrayList<String> list = new ArrayList<>();
        if (!StringUtil.isBlank(ruleEngineEntity.getFromDestination())) {
            list.add(ruleEngineEntity.getFromDestination().trim());
        }

        if (!StringUtil.isBlank(ruleEngineEntity.getToDestination())) {
            list.add(ruleEngineEntity.getToDestination().trim());
        }
        if (!StringUtil.isBlank(ruleEngineEntity.getErrorDestination())) {
            list.add(ruleEngineEntity.getErrorDestination().trim());
        }
        Set<String> topicSet = new HashSet<>(list);
        if (topicSet.size() < list.size()) {
            throw new GovernanceException("source topic 、destination topic and error topic cannot be the same");
        }
    }

    private String getSqlJson(RuleEngineConditionEntity engineConditionEntity) {
        Map<String, String> map = new HashMap<>();
        map.put("connectionOperator", engineConditionEntity.getConnectionOperator());
        map.put("columnName", engineConditionEntity.getColumnName());
        map.put("conditionalOperator", engineConditionEntity.getConditionalOperator());
        map.put("sqlCondition", engineConditionEntity.getSqlCondition());
        return JSONObject.toJSONString(map);
    }

    @SuppressWarnings("unchecked")
    private void updateProcessRule(HttpServletRequest request, RuleEngineEntity ruleEngineEntity, RuleEngineEntity oldRule) throws GovernanceException {
        try {

            String url = new StringBuffer(this.getProcessorUrl()).append(ConstantProperties.PROCESSOR_UPDATE_CEP_RULE).toString();
            String jsonString = JSONObject.toJSONString(ruleEngineEntity);
            Map map = JSONObject.parseObject(jsonString, Map.class);
            map.put("updatedTime", ruleEngineEntity.getLastUpdate());
            map.put("createdTime", oldRule.getCreateDate());
            //updateCEPRuleById
            log.info("update rule begin====map:{}", JSONObject.toJSONString(map));
            CloseableHttpResponse closeResponse = commonService.getCloseResponse(request, url, JSONObject.toJSONString(map));
            //deal processor result
            int statusCode = closeResponse.getStatusLine().getStatusCode();
            if (200 != statusCode) {
                throw new GovernanceException(ErrorCode.PROCESS_CONNECT_ERROR);
            }
            String updateMes = EntityUtils.toString(closeResponse.getEntity());
            JSONObject jsonObject = JSONObject.parseObject(updateMes);
            Integer code = Integer.valueOf(jsonObject.get("errorCode").toString());
            if (PROCESSOR_SUCCESS_CODE != code) {
                String msg = jsonObject.get("errorMsg").toString();
                throw new GovernanceException(msg);
            }
        } catch (Exception e) {
            log.error("processor update ruleEngine fail", e);
            throw new GovernanceException("processor update ruleEngine fail", e);
        }

    }


    private String getConditionFieldDetail(List<RuleEngineConditionEntity> ruleEngineConditionList) {
        if (CollectionUtils.isEmpty(ruleEngineConditionList)) {
            return null;
        }
        String blank = " ";
        StringBuffer buffer = new StringBuffer(blank);
        int count = 0;
        for (RuleEngineConditionEntity entity : ruleEngineConditionList) {
            if (count == 0) {
                buffer.append(blank).append(entity.getColumnName()).append(blank)
                        .append(entity.getConditionalOperator().toUpperCase()).append(blank).append(entity.getSqlCondition()).append(blank);
            } else {
                buffer.append(entity.getConnectionOperator().toUpperCase()).append(blank).append(entity.getColumnName()).append(blank)
                        .append(entity.getConditionalOperator().toUpperCase()).append(blank).append(entity.getSqlCondition()).append(blank);
            }
            count++;
        }
        return buffer.toString();
    }

    private String getConditionField(List<RuleEngineConditionEntity> ruleEngineConditionList) {
        if (CollectionUtils.isEmpty(ruleEngineConditionList)) {
            return null;
        }
        String blank = " ";
        StringBuffer buffer = new StringBuffer(blank);
        int count = 0;
        for (RuleEngineConditionEntity entity : ruleEngineConditionList) {
            boolean realNumber = NumberValidationUtils.isRealNumber(entity.getSqlCondition());
            String condtion = entity.getSqlCondition();
            if (!realNumber) {
                condtion = "\"" + condtion + "\"";
            }
            if (count == 0) {
                buffer.append(blank).append(entity.getColumnName())
                        .append(entity.getConditionalOperator()).append(condtion).append(blank);

            } else {
                buffer.append(entity.getConnectionOperator()).append(blank).append(entity.getColumnName())
                        .append(entity.getConditionalOperator()).append(condtion).append(blank);
            }
            count++;
        }
        return buffer.toString();
    }

    @Transactional(rollbackFor = Throwable.class)
    public boolean updateRuleEngineStatus(RuleEngineEntity ruleEngineEntity, HttpServletRequest request, HttpServletResponse response)
            throws GovernanceException {
        authCheck(ruleEngineEntity, request);
        RuleEngineEntity rule = new RuleEngineEntity();
        rule.setId(ruleEngineEntity.getId());
        List<RuleEngineEntity> ruleEngines = ruleEngineMapper.getRuleEngines(rule);
        rule = ruleEngines.get(0);
        BeanUtils.copyProperties(rule, ruleEngineEntity, "status");
        //set payload
        String payload = JSONObject.toJSON(ruleEngineEntity.getPayloadMap()).toString();
        ruleEngineEntity.setPayload(payload);
        ruleEngineEntity.setLastUpdate(new Date());

        //set selectFiled 、conditionField
        List<RuleEngineConditionEntity> ruleEngineConditionList = this.getRuleEngineConditionList(ruleEngineEntity);
        String conditionField = this.getConditionField(ruleEngineConditionList);
        log.info("condition:{}", conditionField);
        ruleEngineEntity.setConditionField(conditionField);
        RuleDatabaseEntity ruleDataBase = getRuleDataBase(ruleEngineEntity.getRuleDataBaseId());
        if (ruleDataBase != null) {
            ruleEngineEntity.setDatabaseUrl(ruleDataBase.getDatabaseUrl() + "&tableName=" + ruleDataBase.getTableName());
            log.info("dataBaseUrl:{}", ruleEngineEntity.getDatabaseUrl());
        }
        this.stopProcessRule(request, ruleEngineEntity, rule);
        return ruleEngineMapper.updateRuleEngineStatus(ruleEngineEntity);
    }

    @SuppressWarnings("unchecked")
    private void stopProcessRule(HttpServletRequest request, RuleEngineEntity ruleEngineEntity, RuleEngineEntity oldRule) throws GovernanceException {
        try {
            BrokerEntity broker = brokerMapper.getBroker(oldRule.getBrokerId());
            ruleEngineEntity.setBrokerUrl(broker.getBrokerUrl());
            String url = new StringBuffer(this.getProcessorUrl()).append(ConstantProperties.PROCESSOR_STOP_CEP_RULE).toString();
            String jsonString = JSONObject.toJSONString(ruleEngineEntity);
            Map map = JSONObject.parseObject(jsonString, Map.class);
            map.put("updatedTime", ruleEngineEntity.getLastUpdate());
            map.put("createdTime", oldRule.getCreateDate());
            //updateCEPRuleById
            log.info("stop rule begin====map:{}", JSONObject.toJSONString(map));
            CloseableHttpResponse closeResponse = commonService.getCloseResponse(request, url, JSONObject.toJSONString(map));
            //deal processor result
            int statusCode = closeResponse.getStatusLine().getStatusCode();
            if (200 != statusCode) {
                throw new GovernanceException(ErrorCode.PROCESS_CONNECT_ERROR);
            }
            String updateMes = EntityUtils.toString(closeResponse.getEntity());
            JSONObject jsonObject = JSONObject.parseObject(updateMes);
            Integer code = Integer.valueOf(jsonObject.get("errorCode").toString());
            if (PROCESSOR_SUCCESS_CODE != code) {
                String msg = jsonObject.get("errorMsg").toString();
                throw new GovernanceException(msg);
            }
        } catch (Exception e) {
            log.error("processor stop ruleEngine fail", e);
            throw new GovernanceException("processor stop ruleEngine fail", e);
        }

    }

    @Transactional(rollbackFor = Throwable.class)
    public boolean startRuleEngine(RuleEngineEntity ruleEngineEntity, HttpServletRequest request, HttpServletResponse response)
            throws GovernanceException {
        RuleEngineEntity rule = new RuleEngineEntity();
        try {
            //query by id and status
            rule.setId(ruleEngineEntity.getId());
            rule.setStatus(StatusEnum.NOT_STARTED.getCode());
            List<RuleEngineEntity> ruleEngines = ruleEngineMapper.getRuleEngines(rule);
            if (CollectionUtils.isEmpty(ruleEngines)) {
                throw new GovernanceException("the data is not non-start state");
            }

            //set required fields
            rule = ruleEngines.get(0);
            rule.setErrorMessage(ERROR_MSG);
            String conditionField = getConditionField(this.getRuleEngineConditionList(rule));
            rule.setConditionField(conditionField);
            log.info("condition:{}", conditionField);

            BrokerEntity broker = brokerMapper.getBroker(rule.getBrokerId());
            rule.setBrokerUrl(broker.getBrokerUrl());
            rule.setStatus(StatusEnum.RUNNING.getCode());
            rule.setLastUpdate(new Date());
            //set dataBaseUrl
            RuleDatabaseEntity ruleDataBase = getRuleDataBase(rule.getRuleDataBaseId());
            if (ruleDataBase != null) {
                rule.setDatabaseUrl(ruleDataBase.getDatabaseUrl() + "&tableName=" + ruleDataBase.getTableName());
                log.info("dataBaseUrl:{}", ruleEngineEntity.getDatabaseUrl());
            }
            //Verify required fields
            this.checkStartRuleRequired(rule);
            //Start the rules engine
            rule.setOffSet(ruleEngineEntity.getOffSet());
            rule.setSystemTag(ruleEngineEntity.getSystemTag());
            this.startProcessRule(request, rule);
            //modify status
            RuleEngineEntity engineEntity = new RuleEngineEntity();
            engineEntity.setId(rule.getId());
            engineEntity.setStatus(StatusEnum.RUNNING.getCode());
            engineEntity.setLastUpdate(rule.getLastUpdate());
            return ruleEngineMapper.updateRuleEngineStatus(engineEntity);
        } catch (Exception e) {
            log.error("start ruleEngine fail", e);
            throw new GovernanceException("start ruleEngine fail", e);
        }
    }

    @SuppressWarnings("unchecked")
    private void startProcessRule(HttpServletRequest request, RuleEngineEntity rule) throws GovernanceException {
        try {
            String jsonString = JSONObject.toJSONString(rule);
            Map map = JSONObject.parseObject(jsonString, Map.class);
            map.put("updatedTime", rule.getLastUpdate());
            map.put("createdTime", rule.getCreateDate());
            String url = new StringBuffer(this.getProcessorUrl()).append(ConstantProperties.PROCESSOR_START_CEP_RULE).toString();
            log.info("start rule begin====map:{}", JSONObject.toJSONString(map));
            CloseableHttpResponse closeResponse = commonService.getCloseResponse(request, url, JSONObject.toJSONString(map));
            int statusCode = closeResponse.getStatusLine().getStatusCode();
            if (200 != statusCode) {
                log.error(ErrorCode.PROCESS_CONNECT_ERROR.getCodeDesc());
                throw new GovernanceException(ErrorCode.PROCESS_CONNECT_ERROR);
            }
            //deal processor result
            String mes = EntityUtils.toString(closeResponse.getEntity());
            JSONObject jsonObject = JSONObject.parseObject(mes);
            Integer code = Integer.valueOf(jsonObject.get("errorCode").toString());
            String msg = jsonObject.get("errorMsg").toString();
            if (PROCESSOR_SUCCESS_CODE != code) {
                throw new GovernanceException(msg);
            }
        } catch (Exception e) {
            log.error("processor start ruleEngine fail,error:{}", e.getMessage());
            throw new GovernanceException("processor start ruleEngine fail! ", e);
        }

    }

    private void checkStartRuleRequired(RuleEngineEntity rule) throws GovernanceException {
        if (StringUtil.isBlank(rule.getRuleName())) {
            log.error("the ruleName is empty");
            throw new GovernanceException("the ruleName is empty");
        }
        if (rule.getUserId() == null) {
            log.error("the userId is empty");
            throw new GovernanceException("the userId is empty");
        }
        if (rule.getBrokerId() == null) {
            log.error("the brokerId is empty");
            throw new GovernanceException("the brokerId is empty");
        }

        if (StringUtil.isBlank(rule.getBrokerUrl())) {
            log.error("the brokerUrl is empty");
            throw new GovernanceException("the brokerUrl is empty");
        }
        this.checkField(rule);
    }

    private void checkField(RuleEngineEntity rule) throws GovernanceException {
        if (rule.getConditionType() == null) {
            log.error("the conditionType is empty");
            throw new GovernanceException("the conditionType is empty");
        }
        boolean flag = ConditionTypeEnum.TOPIC.getCode().intValue() == rule.getConditionType().intValue() && StringUtil.isBlank(rule.getToDestination());
        if (flag) {
            log.error("the toDestination is empty");
            throw new GovernanceException("the toDestination is empty");
        }
        flag = ConditionTypeEnum.DATABASE.getCode().intValue() == rule.getConditionType().intValue() && StringUtil.isBlank(rule.getDatabaseUrl());
        if (flag) {
            log.error("the databaseUrl is empty");
            throw new GovernanceException("the databaseUrl is empty");
        }
        if (StringUtil.isBlank(rule.getFromDestination())) {
            log.error("the fromDestination is empty");
            throw new GovernanceException("the fromDestination is empty");
        }
    }

    @SuppressWarnings("unchecked")
    public RuleEngineEntity getRuleEngineDetail(RuleEngineEntity ruleEngineEntity, HttpServletRequest request, HttpServletResponse response) throws GovernanceException {
        String accountid = cookiesTools.getCookieValueByName(request, ConstantProperties.COOKIE_MGR_ACCOUNT_ID);
        Boolean flag = permissionService.verifyPermissions(ruleEngineEntity.getBrokerId(), accountid);
        if (!flag) {
            throw new GovernanceException(ErrorCode.ACCESS_DENIED);
        }
        RuleEngineEntity rule = new RuleEngineEntity();
        rule.setId(ruleEngineEntity.getId());
        List<RuleEngineEntity> ruleEngines = ruleEngineMapper.getRuleEngines(rule);
        if (CollectionUtils.isEmpty(ruleEngines)) {
            return null;
        }
        //get sql
        RuleEngineEntity engineEntity = ruleEngines.get(0);

        String payload = engineEntity.getPayload();
        engineEntity.setPayloadMap(payload == null ? new HashMap<>() : JSONObject.parseObject(payload, Map.class));

        RuleEngineConditionEntity ruleEngineConditionEntity = new RuleEngineConditionEntity();
        ruleEngineConditionEntity.setRuleId(engineEntity.getId());
        //get ruleEngineConditionList
        List<RuleEngineConditionEntity> ruleEngineConditionEntities = this.getRuleEngineConditionList(engineEntity);
        engineEntity.setRuleEngineConditionList(ruleEngineConditionEntities == null ? new ArrayList<>() : ruleEngineConditionEntities);
        engineEntity.setConditionField(this.getConditionFieldDetail(ruleEngineConditionEntities));
        String fullSql = parsingDetailSQL(engineEntity);
        engineEntity.setFullSQL(fullSql);
        return engineEntity;
    }

    private String parsingDetailSQL(RuleEngineEntity engineEntity) {
        StringBuffer buffer = new StringBuffer();
        if (StringUtil.isBlank(engineEntity.getFromDestination())) {
            return null;
        }
        RuleEngineConditionEntity ruleEngineConditionEntity = new RuleEngineConditionEntity();
        ruleEngineConditionEntity.setRuleId(engineEntity.getId());
        //get ruleEngineConditionList
        List<RuleEngineConditionEntity> ruleEngineConditionEntities = ruleEngineConditionMapper.ruleEngineConditionList(ruleEngineConditionEntity);
        String selectField = StringUtil.isBlank(engineEntity.getSelectField()) ? ConstantProperties.ASTERISK : engineEntity.getSelectField();
        buffer.append("SELECT ").append(selectField).append(" FROM").append(" ").append(engineEntity.getFromDestination());
        if (!CollectionUtils.isEmpty(ruleEngineConditionEntities)) {
            buffer.append(" WHERE ").append(engineEntity.getConditionField());
        }

        return buffer.toString();
    }

    private void checkRule(RuleEngineEntity ruleEngineEntity) throws GovernanceException {
        //Verify ruleName English letters, numbers, underscores, hyphens,length
        if (StringUtil.isBlank(ruleEngineEntity.getRuleName())) {
            throw new GovernanceException("ruleName is empty");
        }

        boolean flag = checkRuleNameRepeat(ruleEngineEntity);
        if (!flag) {
            throw new GovernanceException("ruleName repeat");
        }
        if (ruleEngineEntity.getPayloadMap().isEmpty()) {
            throw new GovernanceException("rule description is empty");
        }
        if (ruleEngineEntity.getPayload() != null && ruleEngineEntity.getPayload().length() > 4096) {
            throw new GovernanceException("rule description length cannot exceed 4096");
        }

    }

    private boolean checkRuleName(String ruleName, String regex) {
        if (StringUtil.isBlank(ruleName)) {
            return false;
        }
        return Pattern.matches(regex, ruleName);
    }

    //check name repeat
    private boolean checkRuleNameRepeat(RuleEngineEntity ruleEngineEntity) {
        RuleEngineEntity rule = new RuleEngineEntity();
        rule.setRuleName(ruleEngineEntity.getRuleName());
        rule.setSystemTag("2");
        List<RuleEngineEntity> ruleEngines = ruleEngineMapper.checkRuleNameRepeat(rule);
        if (CollectionUtils.isEmpty(ruleEngines)) {
            return true;
        }
        if (ruleEngineEntity.getId() != null && ruleEngines.get(0).getId().intValue() == ruleEngineEntity.getId().intValue()) {
            return true;
        }
        return false;
    }

    private void authCheck(RuleEngineEntity ruleEngineEntity, HttpServletRequest request) throws GovernanceException {
        String accountId = cookiesTools.getCookieValueByName(request, ConstantProperties.COOKIE_MGR_ACCOUNT_ID);
        Boolean flag = permissionService.verifyPermissions(ruleEngineEntity.getBrokerId(), accountId);
        if (!flag) {
            throw new GovernanceException(ErrorCode.ACCESS_DENIED);
        }
    }

    private void checkSqlCondition(RuleEngineConditionEntity conditionEntity) throws GovernanceException {
        //check  empty
        boolean flag = StringUtil.isBlank(conditionEntity.getColumnName())
                || StringUtil.isBlank(conditionEntity.getConditionalOperator()) || StringUtil.isBlank(conditionEntity.getSqlCondition());
        if (flag) {
            throw new GovernanceException("conditional row field cannot be empty");
        }
        //check number
        if (!"==".equals(conditionEntity.getConditionalOperator()) && !"!=".equals(conditionEntity.getConditionalOperator())) {
            boolean matches = NumberValidationUtils.isRealNumber(conditionEntity.getSqlCondition());
            if (!matches) {
                throw new GovernanceException("sqlCondition is not number");
            }
        }
    }

    private String getProcessorUrl() {
        return processorUrl;
    }

    private List<RuleEngineConditionEntity> getRuleEngineConditionList(RuleEngineEntity rule) {
        RuleEngineConditionEntity ruleEngineConditionEntity = new RuleEngineConditionEntity();
        ruleEngineConditionEntity.setRuleId(rule.getId());
        List<RuleEngineConditionEntity> ruleEngineConditionEntities = ruleEngineConditionMapper.ruleEngineConditionList(ruleEngineConditionEntity);
        if (!CollectionUtils.isEmpty(ruleEngineConditionEntities)) {
            for (RuleEngineConditionEntity engineConditionEntity : ruleEngineConditionEntities) {
                RuleEngineConditionEntity entity = JSONObject.parseObject(engineConditionEntity.getSqlConditionJson(), RuleEngineConditionEntity.class);
                engineConditionEntity.setConditionalOperator(entity.getConditionalOperator());
                engineConditionEntity.setConnectionOperator(entity.getConnectionOperator());
                engineConditionEntity.setColumnName(entity.getColumnName());
                engineConditionEntity.setSqlCondition(entity.getSqlCondition());
            }
        }
        return ruleEngineConditionEntities;
    }

    public boolean validationConditions(HttpServletRequest request, RuleEngineEntity ruleEngineEntity) throws GovernanceException {
        if (StringUtil.isBlank(ruleEngineEntity.getConditionField())) {
            return true;
        }
        try {
            String payload = ruleEngineEntity.getPayload();
            String condition = ruleEngineEntity.getConditionField();
            String url = new StringBuffer(this.getProcessorUrl()).append(ConstantProperties.PROCESSOR_CHECK_WHERE_CONDITION)
                    .append("?").append("payload=").append(URLEncoder.encode(payload, "UTF-8"))
                    .append("&condition=").append(URLEncoder.encode(condition, "UTF-8"))
                    .toString();

            CloseableHttpResponse closeResponse = commonService.getCloseResponse(request, url);
            int statusCode = closeResponse.getStatusLine().getStatusCode();
            if (200 != statusCode) {
                throw new GovernanceException(ErrorCode.PROCESS_CONNECT_ERROR);
            }
            String msg = EntityUtils.toString(closeResponse.getEntity());
            JSONObject jsonObject = JSONObject.parseObject(msg);
            Integer code = Integer.valueOf(jsonObject.get("errorCode").toString());
            if (PROCESSOR_SUCCESS_CODE != code) {
                throw new GovernanceException(msg);
            }
            return true;
        } catch (Exception e) {
            log.error("check condition fail", e);
            throw new GovernanceException(e.getMessage());
        }

    }

    public boolean checkProcessorExist(HttpServletRequest request) {
        try {
            String payload = "{\\\"a\\\":1,\\\"b\\\":\\\"test\\\",\\\"c\\\":10}";
            String condition = "\"c<100\"";
            String url = new StringBuffer(this.getProcessorUrl()).append(ConstantProperties.PROCESSOR_CHECK_WHERE_CONDITION)
                    .append(ConstantProperties.QUESTION_MARK).append("payload=").append(URLEncoder.encode(payload, "UTF-8"))
                    .append("&condition=").append(URLEncoder.encode(condition, "UTF-8"))
                    .toString();

            CloseableHttpResponse closeResponse = commonService.getCloseResponse(request, url);
            int statusCode = closeResponse.getStatusLine().getStatusCode();
            return 200 == statusCode;
        } catch (Exception e) {
            log.error("check condition fail", e);
            return false;
        }
    }

    private RuleDatabaseEntity getRuleDataBase(Integer id) {
        return ruleDatabaseMapper.getRuleDataBaseById(id);
    }

    private boolean verifyInfiniteLoop(RuleEngineEntity ruleEngineEntity) {
        if (!ConditionTypeEnum.TOPIC.getCode().equals(ruleEngineEntity.getConditionType())) {
            return true;
        }
        RuleEngineEntity rule = new RuleEngineEntity();
        rule.setGroupId(ruleEngineEntity.getGroupId());
        rule.setBrokerId(ruleEngineEntity.getBrokerId());

        //query all historical rules according to brokerId groupId
        List<RuleEngineEntity> ruleTopicList = ruleEngineMapper.getRuleTopicList(rule);
        if (CollectionUtils.isEmpty(ruleTopicList)) {
            return true;
        }
        ruleTopicList.add(ruleEngineEntity);
        Map<String, Set<String>> topicMap = ruleTopicList.stream().collect(Collectors.groupingBy(RuleEngineEntity::getFromDestination, Collectors.mapping(RuleEngineEntity::getToDestination, Collectors.toSet())));
        return dagDetectUtil.checkLoop(topicMap);
    }


}
