package com.webank.weevent.processor.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.webank.weevent.processor.cache.CEPRuleCache;
import com.webank.weevent.processor.mapper.CEPRuleMapper;
import com.webank.weevent.processor.model.CEPRule;
import com.webank.weevent.processor.model.CEPRuleExample;
import com.webank.weevent.processor.utils.Constants;
import com.webank.weevent.processor.utils.RetCode;
import com.webank.weevent.processor.utils.Util;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.IWeEventClient;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

@Slf4j
@Service
public class CEPRuleServiceImpl implements CEPRuleService {
    //  generator the id
    private volatile int guid = 100;

    @Override
    public List<CEPRule> getCEPRuleListByPage(int currPage, int pageSize) {
        Map<String, Integer> data = new HashMap();
        data.put("currIndex", (currPage - 1) * pageSize);
        data.put("pageSize", pageSize);
        return cepRuleMapper.getCEPRuleListByPage(data);
    }

    @Autowired
    private CEPRuleMapper cepRuleMapper;


    @Override
    public int getCountByCondition(CEPRuleExample cEPRuleExample) {
        return new Long(cepRuleMapper.countByExample(cEPRuleExample)).intValue();
    }

    @Override
    public List<CEPRule> getRulesByBrokerId(String brokerId) {
        return cepRuleMapper.getRulesByBrokerId(brokerId);    }

    @Override
    public List<CEPRule> getRulesByUserId(String userId) {
        return cepRuleMapper.getRulesByUserId(userId);
    }

    @Override
    public List<CEPRule> getRulesByUserIdAndBroker(String userId, String brokerId) {
        return cepRuleMapper.getRulesByUserIdAndBroker(userId,brokerId);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public RetCode setCEPRule(String id, int type) {
        CEPRule rule = cepRuleMapper.selectByPrimaryKey(id);
        //0-->1-->2
        if (rule.getStatus().equals(Constants.RULE_STATUS_DELETE)) {
            return Constants.ALREADY_DELETE;
        }

        if (type == Constants.RULE_STATUS_START) {
            rule.setStatus(Constants.RULE_STATUS_START);// 1 is represent start status
        }

        if (type == Constants.RULE_STATUS_DELETE) {
            rule.setStatus(Constants.RULE_STATUS_DELETE);// 2 is represent delete status
        }
        int ret = cepRuleMapper.updateByPrimaryKeySelective(rule); // 1 success
        if (1 != ret) {
            return Constants.FAIL;
        }

        try {
            if (type == Constants.RULE_STATUS_START) {
                updateCache(cepRuleMapper.selectByPrimaryKey(id), "setstatus");
            }
        } catch (BrokerException e) {
            log.info(e.toString());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
        return Constants.SUCCESS;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public RetCode setCEPRule(CEPRule rule) {
        // check all the field
        try {
            if (!(checkField(rule)).getErrorMsg().equals("success")) {
                return Constants.FAIL;
            }

        } catch (Exception e) {
            log.error(e.toString());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }

        rule.setStatus(1); //default the status
        // update the db
        int ret = cepRuleMapper.updateByPrimaryKeySelective(rule); // 1 is success
        if (1 != ret) {
            return Constants.FAIL;
        }

        // update the cache
        try {
            updateCache(cepRuleMapper.selectByPrimaryKey(rule.getId()), "start");
        } catch (BrokerException e) {
            log.info(e.toString());
        }
        return Constants.SUCCESS;
    }

    @Override
    public List<CEPRule> selectByRuleName(String ruleName) {
        return cepRuleMapper.selectByRuleName(ruleName);
    }

    @Override
    public CEPRule selectByPrimaryKey(String id) {
        return cepRuleMapper.selectByPrimaryKey(id);
    }


    @Override
    public RetCode updateByPrimaryKey(CEPRule record) {
        //check  ruleName、payloay、selectField、conditionField、conditionType、fromDestination、toDestination、databaseUrl
        RetCode ret = checkField(record);
        if (!ret.getErrorCode().equals(Constants.RULE_STATUS_START)) {
            return Constants.FAIL;
        }
        int count = cepRuleMapper.updateByPrimaryKey(record);
        if (count > 0) {
            return Constants.SUCCESS;
        }
        return Constants.FAIL;
    }


    @Override
    public RetCode updateByPrimaryKeySelective(CEPRule record) {
        // check rule name
        if (StringUtils.isBlank(record.getRuleName()) || record.getRuleName().isEmpty()) {
            return Constants.FAIL;
        }
        // update the record
        int count = cepRuleMapper.updateFieldById(record);
        if (count > 0) {
            return Constants.SUCCESS;
        }
        return Constants.FAIL;
    }

    @Override
    public List<CEPRule> getCEPRuleList(String ruleName) {
        return cepRuleMapper.getCEPRuleList(ruleName);
    }


    @Override
    public String insert(CEPRule record) {
        // check the rule name
        if (StringUtils.isBlank(record.getRuleName()) || record.getRuleName().isEmpty()) {
            return "-1";
        }
        // gennerate the id
        record.setId(getGuid());
        record.setStatus(0); //default the status

        // instert the rule
        int ret = cepRuleMapper.insert(record);
        if (ret != 1) {
            return "-1";
        }

        return record.getId();
    }

    private static boolean isJSONValid(String test) {
        try {
            JSONObject.parseObject(test);
        } catch (JSONException ex) {
            try {
                JSONObject.parseArray(test);
            } catch (JSONException ex1) {
                return false;
            }
        }
        return true;

    }

    /**
     * check  ruleName、payloay、selectField、conditionField、conditionType、fromDestination、toDestination、databaseUrl
     *
     * @param record on single record
     * @return return code and message
     */
    private RetCode checkField(CEPRule record) {
        // checkPayload
        try {
            if (record == null) {
                return Constants.RULE_IS_NOT_VALID;
            }
            if (StringUtils.isBlank(record.getRuleName()) || record.getRuleName().isEmpty()) {
                return Constants.RULENAME_IS_BLANK;
            }
            if(StringUtils.isBlank(record.getUserId()) || record.getUserId().isEmpty()){
                return Constants.USERID_IS_BLANK;
            }

            if(StringUtils.isBlank(record.getBrokerId()) || record.getBrokerId().isEmpty()){
                return Constants.BROKERID_IS_BLANK;
            }
            String payload = record.getPayload();
            if (payload.isEmpty() || StringUtils.isBlank(payload)) {
                return Constants.PAYLOAD_IS_BLANK;
            } else {
                //check relation between payloay and selectField
                if (!isJSONValid(payload)) {
                    return Constants.PAYLOAD_ISNOT_JSON;
                }
            }
            if (record.getConditionType() != 1 && record.getConditionType() != 2) {
                return Constants.CONDITIONTYPE_ISNOT_VALID;
            } else {
                // check the topic is exist or not
                if (1 == record.getConditionType()) {
                    boolean temp = checkTopic(record.getToDestination(), record.getBrokerUrl());
                    if (!temp) {
                        log.info("the topic is not exist");
                        return Constants.TOPIC_ISNOT_EXIST;
                    }
                }
                // check the databaseUrl,check is valid (http://...?account=**&password=**)
                if (2 == record.getConditionType()) {
                    boolean temp = checkDatabase(record.getDatabaseUrl());
                    if (!temp) {
                        log.info("database url is wrong");
                        return Constants.URL_ISNOT_VALID;
                    }
                }

            }

            // check topic and check the broker
            if (!checkTopic(record.getFromDestination(), record.getBrokerUrl())) {
                log.info("the topic is not exist");
                return Constants.TOPIC_ISNOT_EXIST;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return Constants.FAIL;
        }
        return Constants.SUCCESS;
    }

    /**
     * check the database url
     *
     * @param databaseUrl database url
     * @return true/false
     * @throws SQLException check connect db
     */
    private Boolean checkDatabase(String databaseUrl) throws SQLException {
        boolean connectUrl = false;
        try {
            Connection conn = Util.getConnection(databaseUrl);
            if (conn != null) {
                connectUrl = true;
                conn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connectUrl;
    }

    /**
     * check the topic
     *
     * @param topicName topic name
     * @param brokerUrl broker url
     * @return true false
     */
    private Boolean checkTopic(String topicName, String brokerUrl) {
        try {
            IWeEventClient weEventClient = IWeEventClient.build(brokerUrl);
            return weEventClient.exist(topicName);
        } catch (BrokerException e) {
            e.printStackTrace();
            return false;
        }
    }


    private String getGuid() {
        guid += 1;
        long now = System.currentTimeMillis();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy");
        String time = dateFormat.format(now);
        String info = now + "";
        int ran = 0;
        if (guid > 999) {
            guid = 100;
        }
        ran = guid;
        return time + info.substring(2, info.length()) + ran;
    }


    // start insert or update
    private void updateCache(CEPRule rule, String handleType) throws BrokerException {
        // update rule map
        if (rule.getStatus() == 2) {
            CEPRuleCache.deleteCEPRuleById(rule);
        } else if ("start".equals(handleType) && rule.getStatus() == 1) {
            // start situation can update the rule
            CEPRuleCache.addOrUpdateCEPRule(rule);
        }
    }
}
