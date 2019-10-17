package com.webank.weevent.governance.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webank.weevent.governance.code.ErrorCode;
import com.webank.weevent.governance.entity.BrokerEntity;
import com.webank.weevent.governance.entity.RuleEngineConditionEntity;
import com.webank.weevent.governance.entity.RuleEngineEntity;
import com.webank.weevent.governance.enums.ConditionTypeEnum;
import com.webank.weevent.governance.enums.PayloadEnum;
import com.webank.weevent.governance.enums.StatusEnum;
import com.webank.weevent.governance.exception.GovernanceException;
import com.webank.weevent.governance.mapper.BrokerMapper;
import com.webank.weevent.governance.mapper.RuleEngineConditionMapper;
import com.webank.weevent.governance.mapper.RuleEngineMapper;
import com.webank.weevent.governance.properties.ConstantProperties;
import com.webank.weevent.governance.utils.CookiesTools;
import com.webank.weevent.governance.utils.NumberValidationUtils;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.jsoup.helper.StringUtil;
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

    @Value("${weevent.processor.port:7008}")
    private String processorPort;

    @Autowired
    private RuleEngineConditionMapper ruleEngineConditionMapper;


    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YYYY-MM-dd hh:mm:ss");


    @Autowired
    private BrokerService brokerService;

    private final String regex = "^[a-z0-9A-Z_-]{1,100}";

    private final static List<String> operatorList = new ArrayList<>(Arrays.asList(">=", "<=", ">", "<", "=", "!="));

    private static final int PROCESSOR_SUCCESS_CODE = 0;

    private final String ERROR_MSG = "success";

    @SuppressWarnings("unchecked")
    public List<RuleEngineEntity> getRuleEngines(HttpServletRequest request, RuleEngineEntity ruleEngineEntity) throws GovernanceException {
        try {
            String accountId = cookiesTools.getCookieValueByName(request, ConstantProperties.COOKIE_MGR_ACCOUNT_ID);

            if (accountId == null || !accountId.equals(ruleEngineEntity.getUserId().toString())) {
                throw new GovernanceException(ErrorCode.ACCESS_DENIED);
            }
            int count = ruleEngineMapper.countRuleEngine(ruleEngineEntity);
            ruleEngineEntity.setTotalCount(count);
            List<RuleEngineEntity> ruleEngineEntities = null;
            if (count > 0) {
                int startIndex = (ruleEngineEntity.getPageNumber() - 1) * ruleEngineEntity.getPageSize();
                int endIndex = ruleEngineEntity.getPageNumber() * ruleEngineEntity.getPageSize();
                ruleEngineEntities = ruleEngineMapper.getRuleEnginePage(ruleEngineEntity, startIndex, endIndex);

                Calendar calendar = Calendar.getInstance();
                for (RuleEngineEntity it : ruleEngineEntities) {
                    calendar.setTime(it.getCreateDate());
                    String format = simpleDateFormat.format(calendar.getTime());
                    it.setCreateDate(simpleDateFormat.parse(format));
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
            //Verify ruleName English letters, numbers, underscores, hyphens,length
            boolean flag = this.checkRuleName(ruleEngineEntity.getRuleName(), this.regex);
            if (!flag) {
                throw new GovernanceException("illegal ruleName format");
            }
            ruleEngineEntity.setStatus(StatusEnum.NOT_STARTED.getCode());
            String payload = JSONObject.toJSON(ruleEngineEntity.getPayloadMap()).toString();
            ruleEngineEntity.setPayload(payload);
            if (ruleEngineEntity.getPayloadType() == null || ruleEngineEntity.getPayloadType() == 0) {
                ruleEngineEntity.setPayloadType(PayloadEnum.JSON.getCode());
            }
            ruleEngineEntity.setCreateDate(new Date());
            ruleEngineEntity.setLastUpdate(new Date());
            ruleEngineEntity.setErrorMessage(this.ERROR_MSG);

            //insert processor
            this.addProcessRule(request, ruleEngineEntity);
            //insert ruleEngine
            ruleEngineMapper.addRuleEngine(ruleEngineEntity);
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
            String brokerUrl = new StringBuffer(broker.getBrokerUrl()).append(ConstantProperties.AND_SYMBOL)
                    .append("groupId=").append(ruleEngineEntity.getGroupId()).toString();
            ruleEngineEntity.setBrokerUrl(brokerUrl);
            String url = new StringBuffer(this.getProcessorUrl(broker.getBrokerUrl())).append(ConstantProperties.PROCESSOR_INSERT).toString();
            String jsonString = JSONObject.toJSONString(ruleEngineEntity);
            Map map = JSONObject.parseObject(jsonString, Map.class);
            map.put("updatedTime", ruleEngineEntity.getLastUpdate());
            map.put("createdTime", ruleEngineEntity.getCreateDate());

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
            if (this.PROCESSOR_SUCCESS_CODE != code) {
                String msg = jsonObject.get("errorMsg").toString();
                throw new GovernanceException(msg);
            }
            String cepId = jsonObject.get("data").toString();
            ruleEngineEntity.setCepId(cepId);
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


    private void deleteProcessRule(HttpServletRequest request, RuleEngineEntity engineEntity) throws GovernanceException {
        try {
            BrokerEntity broker = brokerService.getBroker(engineEntity.getBrokerId());
            String brokerUrl = broker.getBrokerUrl();
            String deleteUrl = new StringBuffer(this.getProcessorUrl(brokerUrl)).append(ConstantProperties.PROCESSOR_DELETE_CEP_RULE).append(ConstantProperties.QUESTION_MARK)
                    .append("id=").append(engineEntity.getCepId()).toString();
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
            if (this.PROCESSOR_SUCCESS_CODE != code) {
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

            RuleEngineEntity rule = new RuleEngineEntity();
            rule.setId(ruleEngineEntity.getId());
            List<RuleEngineEntity> ruleEngines = ruleEngineMapper.getRuleEngines(rule);
            rule = ruleEngines.get(0);

            //get payload
            String payload = JSONObject.toJSON(ruleEngineEntity.getPayloadMap()).toString();
            ruleEngineEntity.setPayload(payload);
            ruleEngineEntity.setLastUpdate(new Date());

            //check databaseUrl
            commonService.checkDataBaseUrl(ruleEngineEntity.getDatabaseUrl());
            //If the selectField is empty, the conditionList is not empty.
            List<RuleEngineConditionEntity> ruleEngineConditionList = ruleEngineEntity.getRuleEngineConditionList();
            String selectField = this.getSelectField(ruleEngineEntity);
            String conditionField = this.getConditionField(ruleEngineConditionList);
            //set selectFiled 、conditionField
            ruleEngineEntity.setSelectField(selectField);
            ruleEngineEntity.setConditionField(conditionField);
            //update process rule
            this.updateProcessRule(request, ruleEngineEntity, rule);

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

    @SuppressWarnings("unchecked")
    private void updateProcessRule(HttpServletRequest request, RuleEngineEntity ruleEngineEntity, RuleEngineEntity oldRule) throws GovernanceException {
        try {
            String cepId = oldRule.getCepId();
            BrokerEntity broker = brokerMapper.getBroker(oldRule.getBrokerId());
            String brokerUrl = new StringBuffer(broker.getBrokerUrl()).append(ConstantProperties.AND_SYMBOL)
                    .append("groupId=").append(ruleEngineEntity.getGroupId()).toString();
            ruleEngineEntity.setBrokerUrl(brokerUrl);
            String url = new StringBuffer(this.getProcessorUrl(broker.getBrokerUrl())).append(ConstantProperties.PROCESSOR_UPDATE_CEP_RULE).toString();
            String jsonString = JSONObject.toJSONString(ruleEngineEntity);
            Map map = JSONObject.parseObject(jsonString, Map.class);
            map.put("id", cepId);
            map.put("updatedTime", ruleEngineEntity.getLastUpdate());
            map.put("createdTime", oldRule.getCreateDate());

            //updateCEPRuleById
            CloseableHttpResponse closeResponse = commonService.getCloseResponse(request, url, JSONObject.toJSONString(map));
            //deal processor result
            int statusCode = closeResponse.getStatusLine().getStatusCode();
            if (200 != statusCode) {
                throw new GovernanceException(ErrorCode.PROCESS_CONNECT_ERROR);
            }
            String updateMes = EntityUtils.toString(closeResponse.getEntity());
            JSONObject jsonObject = JSONObject.parseObject(updateMes);
            Integer code = Integer.valueOf(jsonObject.get("errorCode").toString());
            if (this.PROCESSOR_SUCCESS_CODE != code) {
                String msg = jsonObject.get("errorMsg").toString();
                throw new GovernanceException(msg);
            }
        } catch (Exception e) {
            log.error("processor start ruleEngine fail");
            throw new GovernanceException("processor start ruleEngine fail");
        }

    }


    private String getConditionField(List<RuleEngineConditionEntity> ruleEngineConditionList) {
        if (CollectionUtils.isEmpty(ruleEngineConditionList)) {
            return null;
        }
        String blank = " ";
        StringBuffer buffer = new StringBuffer(blank);
        for (RuleEngineConditionEntity entity : ruleEngineConditionList) {
            buffer.append(entity.getConnectionOperator()).append(blank).append(entity.getColumnName()).append(blank)
                    .append(entity.getConditionalOperator()).append(blank).append(entity.getSqlCondition()).append(blank);
        }
        return buffer.toString();
    }

    private String getSelectField(RuleEngineEntity ruleEngineEntity) {
        if (!StringUtil.isBlank(ruleEngineEntity.getSelectField())) {
            return ruleEngineEntity.getSelectField();
        }
        String payload = ruleEngineEntity.getPayload();
        JSONObject jsonObject = JSONObject.parseObject(payload);
        Set<String> set = jsonObject.keySet();
        return String.join(",", set);
    }


    @Transactional(rollbackFor = Throwable.class)
    public boolean updateRuleEngineStatus(RuleEngineEntity ruleEngineEntity, HttpServletRequest request, HttpServletResponse response)
            throws GovernanceException {
        authCheck(ruleEngineEntity, request);
        return ruleEngineMapper.updateRuleEngineStatus(ruleEngineEntity);
    }

    @Transactional(rollbackFor = Throwable.class)
    @SuppressWarnings("unchecked")
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
            rule.setConditionField(getConditionField(this.getRuleEngineConditionList(rule)));
            rule.setSelectField(this.getSelectField(rule));
            BrokerEntity broker = brokerMapper.getBroker(rule.getBrokerId());
            String brokerUrl = new StringBuffer(broker.getBrokerUrl()).append(ConstantProperties.AND_SYMBOL)
                    .append("groupId=").append(ruleEngineEntity.getGroupId()).toString();
            ruleEngineEntity.setBrokerUrl(brokerUrl);
            rule.setStatus(StatusEnum.RUNNING.getCode());
            rule.setLastUpdate(new Date());
            //Verify required fields
            checkRule(rule);
            //Start the rules engine
            this.startProcessRule(request, rule);
            //modify status
            RuleEngineEntity engineEntity = new RuleEngineEntity();
            engineEntity.setId(rule.getId());
            ruleEngineEntity.setStatus(StatusEnum.RUNNING.getCode());
            ruleEngineEntity.setLastUpdate(rule.getLastUpdate());
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
            map.put("id", rule.getCepId());
            map.put("updatedTime", rule.getLastUpdate());
            map.put("createdTime", rule.getCreateDate());
            String url = new StringBuffer(this.getProcessorUrl(rule.getBrokerUrl())).append(ConstantProperties.PROCESSOR_START_CEP_RULE).toString();
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
            if (this.PROCESSOR_SUCCESS_CODE != code) {
                throw new GovernanceException(msg);
            }
        } catch (Exception e) {
            log.error("processor start ruleEngine fail,error:{}", e.getMessage());
            throw new GovernanceException("processor start ruleEngine fail! ", e);
        }

    }

    private void checkRule(RuleEngineEntity rule) throws GovernanceException {
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
        if (StringUtil.isBlank(rule.getPayload())) {
            log.error("the payload is empty");
            throw new GovernanceException("the payload is empty");
        }
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
        if (flag) {
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
        List<RuleEngineConditionEntity> ruleEngineConditionEntities = ruleEngineConditionMapper.ruleEngineConditionList(ruleEngineConditionEntity);
        engineEntity.setRuleEngineConditionList(ruleEngineConditionEntities == null ? new ArrayList<>() : ruleEngineConditionEntities);
        engineEntity.setConditionField(this.getConditionField(ruleEngineConditionEntities));
        String fullSql = parsingSQL(engineEntity);
        engineEntity.setFullSQL(fullSql);
        return engineEntity;
    }

    private String parsingSQL(RuleEngineEntity engineEntity) {
        StringBuffer buffer = new StringBuffer();
        if (StringUtil.isBlank(engineEntity.getFromDestination())) {
            return null;
        }
        RuleEngineConditionEntity ruleEngineConditionEntity = new RuleEngineConditionEntity();
        ruleEngineConditionEntity.setRuleId(engineEntity.getId());
        //get ruleEngineConditionList
        List<RuleEngineConditionEntity> ruleEngineConditionEntities = ruleEngineConditionMapper.ruleEngineConditionList(ruleEngineConditionEntity);
        String selectField = StringUtil.isBlank(engineEntity.getSelectField()) ? ConstantProperties.ASTERISK : engineEntity.getSelectField();
        buffer.append("select ").append(engineEntity.getSelectField()).append(selectField).append(" from").append(" ").append(engineEntity.getFromDestination());
        if (!CollectionUtils.isEmpty(ruleEngineConditionEntities)) {
            buffer.append(" where ").append(engineEntity.getConditionField());
        }

        return buffer.toString();
    }

    private boolean checkRuleName(String ruleName, String regex) {
        if (ruleName == null || ruleName.trim().length() == 0) {
            return false;
        }
        return Pattern.matches(regex, ruleName);

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
        boolean flag = StringUtil.isBlank(conditionEntity.getConditionalOperator()) || StringUtil.isBlank(conditionEntity.getColumnName())
                || StringUtil.isBlank(conditionEntity.getConnectionOperator()) || StringUtil.isBlank(conditionEntity.getSqlCondition());
        if (flag) {
            throw new GovernanceException("conditional row field cannot be empty");
        }
        //check number
        if (operatorList.contains(conditionEntity.getConditionalOperator())) {
            boolean matches = NumberValidationUtils.isRealNumber(conditionEntity.getSqlCondition());
            if (!matches) {
                throw new GovernanceException("sqlCondition is not number");
            }
        }

    }

    private String getProcessorUrl(String brokerUrl) {
        String ip = brokerUrl.substring(brokerUrl.indexOf("//") + 2, brokerUrl.lastIndexOf(":"));
        return new StringBuffer(commonService.HTTP).append(":").append("//").append(ip).append(":")
                .append(this.processorPort).append("/weevent").toString();
    }

    private List<RuleEngineConditionEntity> getRuleEngineConditionList(RuleEngineEntity rule) {
        RuleEngineConditionEntity ruleEngineConditionEntity = new RuleEngineConditionEntity();
        ruleEngineConditionEntity.setRuleId(rule.getId());
        List<RuleEngineConditionEntity> ruleEngineConditionEntities = ruleEngineConditionMapper.ruleEngineConditionList(ruleEngineConditionEntity);
        return ruleEngineConditionEntities;
    }

}
