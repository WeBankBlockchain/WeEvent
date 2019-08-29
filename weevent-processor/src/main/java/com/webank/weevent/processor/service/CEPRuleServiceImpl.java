package com.webank.weevent.processor.service;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import com.webank.weevent.processor.mapper.CEPRuleMapper;
import com.webank.weevent.processor.model.CEPRule;
import com.webank.weevent.processor.model.CEPRuleExample;
import com.webank.weevent.processor.utils.Constants;
import com.webank.weevent.processor.utils.RetCode;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.IWeEventClient;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CEPRuleServiceImpl implements CEPRuleService {

    @Autowired
    CEPRuleMapper cepRuleMapper;


    @Override
    public int getCountByCondition(CEPRuleExample cEPRuleExample) {
        return new Long(cepRuleMapper.countByExample(cEPRuleExample)).intValue();
    }

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
        int ret = cepRuleMapper.updateByPrimaryKeySelective(rule);
        if (ret != Constants.SUCCESS_CODE) {
            return Constants.FAIL;
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
        checkField(record);
        int count = cepRuleMapper.updateByPrimaryKey(record);
        if (count > 0) {
            return Constants.SUCCESS;
        }
        return Constants.FAIL;
    }


    @Override
    public RetCode updateByPrimaryKeySelective(CEPRule record) {
        // check the fields

        int count = cepRuleMapper.updateByPrimaryKeySelective(record);
        if (count > 0) {
            return Constants.SUCCESS;
        }
        return Constants.FAIL;
    }

    @Override
    public List<CEPRule> getCEPRuleList(String ruleName) {
        List<CEPRule> CEPRuleList = cepRuleMapper.getCEPRuleList(ruleName);
        return CEPRuleList;
    }


    @Override
    public RetCode insert(CEPRule record) {
        // check all the field
        checkField(record);

        record.setId(getGuid());
        record.setStatus(0); //default the status
        Integer ret = cepRuleMapper.insert(record);
        if (ret != 1) {
            return Constants.INSERT_RECORD_FAIL;
        }
        return Constants.SUCCESS;
    }

    /**
     *     check  ruleName、payloay、selectField、conditionField、conditionType、fromDestination、toDestination、databaseUrl
     * @param record
     * @return
     */
    private RetCode checkField(CEPRule record) {
        // checkPayload
        if (StringUtils.isBlank(record.getRuleName()) || record.getRuleName().isEmpty()) {
            return Constants.RULENAME_IS_BLANK;
        }
        String payload = record.getPayload();
        if (payload.isEmpty() || StringUtils.isBlank(payload)) {
            return Constants.PAYLOAD_IS_BLANK;
        }else{
            //check relation between payloay and selectField
        }
        // check payloay
        boolean isRight = isJSON2(record.getPayload());
        if (!isRight) {
            return Constants.PAYLOAD_ISNOT_JSON;
        }
        // check topic and check the broker
        if (!checkTopic(record.getFromDestination())) {
            log.info("the topic is not exist");
            return Constants.TOPIC_ISNOT_EXIST;
        }
        if (!checkTopic(record.getToDestination())) {
            log.info("the topic is not exist");
            return Constants.TOPIC_ISNOT_EXIST;
        }
        // check broker
        if(!isHttpUrl(record.getBrokerUrl())){
            log.info("broker url is wrong");
            return Constants.URL_ISNOT_VALID;
        }
        // check the databaseUrl,check is valid (http://...?account=**&password=**)
        if(!isHttpUrl(record.getDatabaseUrl())){
            log.info("database url is wrong");
            return Constants.URL_ISNOT_VALID;
        }

        return Constants.SUCCESS;
    }

    private  boolean isHttpUrl(String urls) {
        boolean isurl = false;
        String regex = "(((https|http)?://)?([a-z0-9]+[.])|(www.))"
                + "\\w+[.|\\/]([a-z0-9]{0,})?[[.]([a-z0-9]{0,})]+((/[\\S&&[^,;\u4E00-\u9FA5]]+)+)?([.][a-z0-9]{0,}+|/?)";//设置正则表达式

        Pattern pat = Pattern.compile(regex.trim());
        Matcher mat = pat.matcher(urls.trim());
        isurl = mat.matches();
        if (isurl) {
            isurl = true;
        }
        return isurl;
    }

    private boolean checkDatabase(String databaseUrl) {
        return false;
    }

    private boolean checkTopic(String topicName) {
        try {
            IWeEventClient weEventClient = IWeEventClient.build("http://182.254.159.91:8090/weevent");
            return weEventClient.exist(topicName);
        } catch (BrokerException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean isJSON2(String str) {
        boolean result = false;
        try {
            Object obj = JSON.parse(str);
            result = true;
        } catch (Exception e) {
            result = false;
        }
        return result;
    }

    //  generator the id
    private volatile int guid = 100;

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

}
