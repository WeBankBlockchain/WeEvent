package com.webank.weevent.governance.service;

import java.io.IOException;
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
import com.webank.weevent.governance.enums.DeleteAtEnum;
import com.webank.weevent.governance.enums.PayloadEnum;
import com.webank.weevent.governance.enums.StatusEnum;
import com.webank.weevent.governance.exception.GovernanceException;
import com.webank.weevent.governance.mapper.RuleEngineMapper;
import com.webank.weevent.governance.properties.ConstantProperties;
import com.webank.weevent.governance.repository.RuleDatabaseRepository;
import com.webank.weevent.governance.repository.RuleEngineConditionRepository;
import com.webank.weevent.governance.repository.RuleEngineRepository;
import com.webank.weevent.governance.utils.CookiesTools;
import com.webank.weevent.governance.utils.DAGDetectUtil;
import com.webank.weevent.governance.utils.JsonUtil;
import com.webank.weevent.governance.utils.NumberValidationUtils;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.jsoup.helper.StringUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Example;
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
    private RuleEngineRepository ruleEngineRepository;

    @Autowired
    private CommonService commonService;

    @Autowired
    private CookiesTools cookiesTools;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private BrokerService brokerService;

    @Autowired
    private DAGDetectUtil dagDetectUtil;


    @Value("${weevent.processor.url:http://127.0.0.1:7008}")
    private String processorUrl;

    @Autowired
    private RuleEngineConditionRepository ruleEngineConditionRepository;

    @Autowired
    private RuleDatabaseRepository ruleDatabaseRepository;

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YYYY-MM-dd hh:mm:ss");

    private static final int PROCESSOR_SUCCESS_CODE = 0;

    @SuppressWarnings("unchecked")
    public List<RuleEngineEntity> getRuleEngines(HttpServletRequest request, RuleEngineEntity ruleEngineEntity) throws GovernanceException {
        try {
            String accountId = cookiesTools.getCookieValueByName(request, ConstantProperties.COOKIE_MGR_ACCOUNT_ID);

            if (accountId == null || !accountId.equals(ruleEngineEntity.getUserId().toString())) {
                throw new GovernanceException(ErrorCode.ACCESS_DENIED);
            }
            ruleEngineEntity.setSystemTag(false);
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
                    it.setPayloadMap(payload == null ? new HashMap<>() : JsonUtil.parseObject(payload, Map.class));
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
            ruleEngineEntity.setSystemTag(false);
            ruleEngineEntity.setStatus(StatusEnum.NOT_STARTED.getCode());
            String payload = JsonUtil.toJSONString(ruleEngineEntity.getPayloadMap());
            ruleEngineEntity.setPayload(payload);
            if (ruleEngineEntity.getPayloadType() == null || ruleEngineEntity.getPayloadType() == 0) {
                ruleEngineEntity.setPayloadType(PayloadEnum.JSON.getCode());
            }
            ruleEngineEntity.setCreateDate(new Date());
            ruleEngineEntity.setLastUpdate(new Date());

            //check rule
            this.checkRule(ruleEngineEntity);
            //insert ruleEngine
            ruleEngineRepository.save(ruleEngineEntity);
            log.info("add end");
            return ruleEngineEntity;
        } catch (Exception e) {
            log.error("add ruleEngineEntity fail", e);
            throw new GovernanceException("add ruleEngineEntity fail ", e);
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
            ruleEngineEntity.setStatus(StatusEnum.IS_DELETED.getCode());

            //delete processor rule
            this.deleteProcessRule(request, engineEntity);
            //delete RuleEngineCondition
            ruleEngineConditionRepository.deleteRuleEngineCondition(ruleEngineEntity.getId());
            //delete RuleEngine
            ruleEngineRepository.deleteRuleEngine(ruleEngineEntity.getId(), new Date().getTime());
            log.info("delete end");
            return true;
        } catch (Exception e) {
            log.error("delete ruleEngineEntity fail", e);
            throw new GovernanceException("delete ruleEngineEntity fail ", e);
        }
    }


    public void deleteProcessRule(HttpServletRequest request, RuleEngineEntity engineEntity) throws GovernanceException {
        try {
            if (!this.checkProcessorExist(request)) {
                return;
            }
            String deleteUrl = new StringBuffer(this.getProcessorUrl()).append(ConstantProperties.PROCESSOR_DELETE_CEP_RULE).append(ConstantProperties.QUESTION_MARK)
                    .append("id=").append(engineEntity.getId()).toString();
            log.info("processor delete  begin");
            CloseableHttpResponse closeResponse = commonService.getCloseResponse(request, deleteUrl);
            String mes = EntityUtils.toString(closeResponse.getEntity());
            log.info("delete rule result:{}", mes);
            //deal processor result
            int statusCode = closeResponse.getStatusLine().getStatusCode();
            if (200 != statusCode) {
                log.error(ErrorCode.PROCESS_CONNECT_ERROR.getCodeDesc());
                throw new GovernanceException(ErrorCode.PROCESS_CONNECT_ERROR);
            }

            Map jsonObject = JsonUtil.parseObject(mes, Map.class);
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
            String payload = JsonUtil.toJSONString(ruleEngineEntity.getPayloadMap());
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
            //set ruleDataBaseUrl
            setRuleDataBaseUrl(ruleEngineEntity);
            //verify source topic and destination topic, errorTopic is different
            checkSourceDestinationTopic(ruleEngineEntity);
            ruleEngineEntity.setStatus(rule.getStatus());

            //update process rule
            BrokerEntity broker = brokerService.getBroker(rule.getBrokerId());
            ruleEngineEntity.setBrokerUrl(broker.getBrokerUrl());
            if (rule.getStatus() == StatusEnum.NOT_STARTED.getCode()) {
                this.updateProcessRule(request, ruleEngineEntity, rule);
            } else {
                ruleEngineEntity.setGroupId(rule.getGroupId());
                ruleEngineEntity.setSystemTag(false);
                this.startProcessRule(request, ruleEngineEntity);
            }

            //delete old ruleEngineConditionEntity
            ruleEngineConditionRepository.deleteRuleEngineCondition(ruleEngineEntity.getId());

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
                ruleEngineConditionRepository.saveAll(ruleEngineConditionList);
            }
            BeanUtils.copyProperties(rule, ruleEngineEntity, "ruleName", "payload", "selectField", "conditionType",
                    "fromDestination", "toDestination", "ruleDataBaseId", "errorDestination");
            ruleEngineRepository.save(ruleEngineEntity);
            log.info("update ruleEngine end");
            return true;
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

    private String getSqlJson(RuleEngineConditionEntity engineConditionEntity) throws IOException {
        Map<String, String> map = new HashMap<>();
        map.put("connectionOperator", engineConditionEntity.getConnectionOperator());
        map.put("columnName", engineConditionEntity.getColumnName());
        map.put("conditionalOperator", engineConditionEntity.getConditionalOperator());
        map.put("sqlCondition", engineConditionEntity.getSqlCondition());
        return JsonUtil.toJSONString(map);
    }

    @SuppressWarnings("unchecked")
    private void updateProcessRule(HttpServletRequest request, RuleEngineEntity ruleEngineEntity, RuleEngineEntity oldRule) throws GovernanceException {
        try {
            if (!this.checkProcessorExist(request)) {
                return;
            }
            String url = new StringBuffer(this.getProcessorUrl()).append(ConstantProperties.PROCESSOR_UPDATE_CEP_RULE).toString();
            String jsonString = JsonUtil.toJSONString(ruleEngineEntity);
            Map map = JsonUtil.parseObject(jsonString, Map.class);
            map.put("updatedTime", ruleEngineEntity.getLastUpdate());
            map.put("createdTime", oldRule.getCreateDate());
            //updateCEPRuleById
            log.info("update rule begin====map:{}", JsonUtil.toJSONString(map));
            CloseableHttpResponse closeResponse = commonService.getCloseResponse(request, url, JsonUtil.toJSONString(map));
            String updateMes = EntityUtils.toString(closeResponse.getEntity());
            log.info("update rule end====result:{}", updateMes);
            //deal processor result
            int statusCode = closeResponse.getStatusLine().getStatusCode();
            if (200 != statusCode) {
                throw new GovernanceException(ErrorCode.PROCESS_CONNECT_ERROR);
            }
            Map jsonObject = JsonUtil.parseObject(updateMes, Map.class);
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
        RuleEngineEntity rule = ruleEngineRepository.findById(ruleEngineEntity.getId());
        BeanUtils.copyProperties(rule, ruleEngineEntity, "status");
        try {
            //set payload
            String payload = JsonUtil.toJSONString(ruleEngineEntity.getPayloadMap());
            ruleEngineEntity.setPayload(payload);
            ruleEngineEntity.setLastUpdate(new Date());

            //set selectFiled 、conditionField
            List<RuleEngineConditionEntity> ruleEngineConditionList = this.getRuleEngineConditionList(ruleEngineEntity);
            String conditionField = this.getConditionField(ruleEngineConditionList);
            log.info("condition:{}", conditionField);
            ruleEngineEntity.setConditionField(conditionField);

            //set ruleDataBaseUrl
            setRuleDataBaseUrl(ruleEngineEntity);

            //stop process
            this.stopProcessRule(request, ruleEngineEntity, rule);

            ruleEngineRepository.save(ruleEngineEntity);
            log.info("update status end");
            return true;
        } catch (Exception e) {
            log.error("update status fail", e);
            throw new GovernanceException("update status fail", e);

        }

    }

    @SuppressWarnings("unchecked")
    private void stopProcessRule(HttpServletRequest request, RuleEngineEntity ruleEngineEntity, RuleEngineEntity oldRule) throws GovernanceException {
        try {
            if (!this.checkProcessorExist(request)) {
                return;
            }
            BrokerEntity broker = brokerService.getBroker(oldRule.getBrokerId());
            ruleEngineEntity.setBrokerUrl(broker.getBrokerUrl());
            String url = new StringBuffer(this.getProcessorUrl()).append(ConstantProperties.PROCESSOR_STOP_CEP_RULE).toString();
            String jsonString = JsonUtil.toJSONString(ruleEngineEntity);
            Map map = JsonUtil.parseObject(jsonString, Map.class);
            map.put("updatedTime", ruleEngineEntity.getLastUpdate());
            map.put("createdTime", oldRule.getCreateDate());
            //updateCEPRuleById
            log.info("stop rule begin====map:{}", JsonUtil.toJSONString(map));
            CloseableHttpResponse closeResponse = commonService.getCloseResponse(request, url, JsonUtil.toJSONString(map));
            String stopMsg = EntityUtils.toString(closeResponse.getEntity());
            log.info("stop rule end====result:{}", stopMsg);
            //deal processor result
            int statusCode = closeResponse.getStatusLine().getStatusCode();
            if (200 != statusCode) {
                throw new GovernanceException(ErrorCode.PROCESS_CONNECT_ERROR);
            }

            Map jsonObject = JsonUtil.parseObject(stopMsg, Map.class);
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
            String conditionField = getConditionField(this.getRuleEngineConditionList(rule));
            rule.setConditionField(conditionField);
            log.info("condition:{}", conditionField);

            BrokerEntity broker = brokerService.getBroker(rule.getBrokerId());
            rule.setBrokerUrl(broker.getBrokerUrl());
            rule.setStatus(StatusEnum.RUNNING.getCode());
            rule.setLastUpdate(new Date());
            //set dataBaseUrl
            setRuleDataBaseUrl(rule);

            //Verify required fields
            this.checkStartRuleRequired(rule);
            //Start the rules engine
            rule.setOffSet(ruleEngineEntity.getOffSet());
            this.startProcessRule(request, rule);
            //modify status
            ruleEngineRepository.save(rule);
            log.info("start ruleEngine end");
            return true;
        } catch (Exception e) {
            log.error("start ruleEngine fail", e);
            throw new GovernanceException("start ruleEngine fail", e);
        }
    }

    @SuppressWarnings("unchecked")
    private void startProcessRule(HttpServletRequest request, RuleEngineEntity rule) throws GovernanceException {
        try {
            if (!this.checkProcessorExist(request)) {
                return;
            }
            String jsonString = JsonUtil.toJSONString(rule);
            Map map = JsonUtil.parseObject(jsonString, Map.class);
            map.put("updatedTime", rule.getLastUpdate());
            map.put("createdTime", rule.getCreateDate());
            String url = new StringBuffer(this.getProcessorUrl()).append(ConstantProperties.PROCESSOR_START_CEP_RULE).toString();
            map.put("systemTag", rule.getSystemTag() ? "1" : "0");
            log.info("start rule begin====map:{}", JsonUtil.toJSONString(map));
            CloseableHttpResponse closeResponse = commonService.getCloseResponse(request, url, JsonUtil.toJSONString(map));
            //deal processor result
            String mes = EntityUtils.toString(closeResponse.getEntity());
            log.info("start rule end====result:{}", mes);
            int statusCode = closeResponse.getStatusLine().getStatusCode();
            if (200 != statusCode) {
                log.error(ErrorCode.PROCESS_CONNECT_ERROR.getCodeDesc());
                throw new GovernanceException(ErrorCode.PROCESS_CONNECT_ERROR);
            }

            Map jsonObject = JsonUtil.parseObject(mes, Map.class);
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
        try{
            String payload = engineEntity.getPayload();
            engineEntity.setPayloadMap(payload == null ? new HashMap<>() : JsonUtil.parseObject(payload, Map.class));
            RuleEngineConditionEntity ruleEngineConditionEntity = new RuleEngineConditionEntity();
            ruleEngineConditionEntity.setRuleId(engineEntity.getId());
            //get ruleEngineConditionList
            List<RuleEngineConditionEntity> ruleEngineConditionEntities = this.getRuleEngineConditionList(engineEntity);
            engineEntity.setRuleEngineConditionList(ruleEngineConditionEntities == null ? new ArrayList<>() : ruleEngineConditionEntities);
            engineEntity.setConditionField(this.getConditionFieldDetail(ruleEngineConditionEntities));
            String fullSql = parsingDetailSQL(engineEntity);
            engineEntity.setFullSQL(fullSql);
            return engineEntity;
        }catch (Exception e){
            log.error("get rule detail fail",e);
            throw new GovernanceException("");
        }

    }

    private String parsingDetailSQL(RuleEngineEntity engineEntity) {
        StringBuffer buffer = new StringBuffer();
        if (StringUtil.isBlank(engineEntity.getFromDestination())) {
            return null;
        }
        RuleEngineConditionEntity ruleEngineConditionEntity = new RuleEngineConditionEntity();
        ruleEngineConditionEntity.setRuleId(engineEntity.getId());
        //get ruleEngineConditionList
        String selectField = StringUtil.isBlank(engineEntity.getSelectField()) ? ConstantProperties.ASTERISK : engineEntity.getSelectField();
        buffer.append("SELECT ").append(selectField).append(" FROM").append(" ").append(engineEntity.getFromDestination());
        if (!StringUtil.isBlank(engineEntity.getConditionField())) {
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
        rule.setSystemTag(false);
        rule.setDeleteAt(DeleteAtEnum.NOT_DELETED.getCode());
        Example<RuleEngineEntity> example = Example.of(rule);
        List<RuleEngineEntity> ruleEngines = ruleEngineRepository.findAll(example);
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

    private List<RuleEngineConditionEntity> getRuleEngineConditionList(RuleEngineEntity rule) throws IOException {
        List<RuleEngineConditionEntity> ruleEngineConditionEntities = ruleEngineConditionRepository.findAllByRuleId(rule.getId());
        if (!CollectionUtils.isEmpty(ruleEngineConditionEntities)) {
            for (RuleEngineConditionEntity engineConditionEntity : ruleEngineConditionEntities) {
                RuleEngineConditionEntity entity = JsonUtil.parseObject(engineConditionEntity.getSqlConditionJson(), RuleEngineConditionEntity.class);
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
            Map jsonObject = JsonUtil.parseObject(msg, Map.class);
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
            return false;
        }
    }

    private void setRuleDataBaseUrl(RuleEngineEntity rule) {
        if (rule.getRuleDataBaseId() == null) {
            return;
        }
        RuleDatabaseEntity ruleDataBase = ruleDatabaseRepository.findById(rule.getRuleDataBaseId());
        if (ruleDataBase != null) {
            String dbUrl = ruleDataBase.getDatabaseUrl() + "?user=" + ruleDataBase.getUsername() + "&password=" + ruleDataBase.getPassword() +
                    "&tableName=" + ruleDataBase.getTableName();
            if (!StringUtil.isBlank(ruleDataBase.getOptionalParameter())) {
                dbUrl = dbUrl + "&" + ruleDataBase.getOptionalParameter();
            }
            rule.setDatabaseUrl(dbUrl);
            rule.setTableName(ruleDataBase.getTableName());
            log.info("dataBaseUrl:{}", rule.getDatabaseUrl());
        }
    }

    private boolean verifyInfiniteLoop(RuleEngineEntity ruleEngineEntity) {
        if (!ConditionTypeEnum.TOPIC.getCode().equals(ruleEngineEntity.getConditionType())) {
            return true;
        }
        //query all historical rules according to brokerId groupId
        List<RuleEngineEntity> ruleTopicList = ruleEngineRepository.getRuleTopicList(ruleEngineEntity.getBrokerId(), ruleEngineEntity.getGroupId());
        if (CollectionUtils.isEmpty(ruleTopicList)) {
            return true;
        }
        ruleTopicList.add(ruleEngineEntity);
        Map<String, Set<String>> topicMap = ruleTopicList.stream().collect(Collectors.groupingBy(RuleEngineEntity::getFromDestination, Collectors.mapping(RuleEngineEntity::getToDestination, Collectors.toSet())));
        return dagDetectUtil.checkLoop(topicMap);
    }

}
