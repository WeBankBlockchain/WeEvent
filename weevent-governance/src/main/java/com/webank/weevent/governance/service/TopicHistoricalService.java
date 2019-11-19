package com.webank.weevent.governance.service;

import java.io.FileInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webank.weevent.governance.code.ConstantCode;
import com.webank.weevent.governance.entity.BrokerEntity;
import com.webank.weevent.governance.entity.RuleDatabaseEntity;
import com.webank.weevent.governance.entity.RuleEngineEntity;
import com.webank.weevent.governance.entity.TopicTopicHistoricalEntity;
import com.webank.weevent.governance.enums.ConditionTypeEnum;
import com.webank.weevent.governance.enums.PayloadEnum;
import com.webank.weevent.governance.enums.StatusEnum;
import com.webank.weevent.governance.exception.GovernanceException;
import com.webank.weevent.governance.mapper.RuleDatabaseMapper;
import com.webank.weevent.governance.mapper.RuleEngineMapper;
import com.webank.weevent.governance.mapper.TopicHistoricalMapper;
import com.webank.weevent.governance.properties.ConstantProperties;
import com.webank.weevent.governance.utils.CookiesTools;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.jsoup.helper.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
@Slf4j
public class TopicHistoricalService {

    @Autowired
    private TopicHistoricalMapper topicHistoricalMapper;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private CookiesTools cookiesTools;

    @Autowired
    private CommonService commonService;

    @Autowired
    private RuleEngineService ruleEngineService;

    @Autowired
    private RuleEngineMapper ruleEngineMapper;

    @Autowired
    private RuleDatabaseMapper ruleDatabaseMapper;

    private final static String simpleDateFormat = "YYYY-MM-dd";

    private final static String TOPIC_HISTORICAL = "t_topic_historical";

    public Map<String, List<Integer>> historicalDataList(TopicTopicHistoricalEntity topicHistoricalEntity, HttpServletRequest httpRequest,
                                                         HttpServletResponse httpResponse) throws GovernanceException {
        try {
            String accountId = cookiesTools.getCookieValueByName(httpRequest, ConstantProperties.COOKIE_MGR_ACCOUNT_ID);
            Boolean flag = permissionService.verifyPermissions(topicHistoricalEntity.getBrokerId(), accountId);
            if (!flag) {
                throw new GovernanceException(ConstantCode.ACCESS_DENIED.getMsg());
            }
            Map<String, List<Integer>> returnMap = new HashMap<>();

            List<TopicTopicHistoricalEntity> historicalDataEntities = topicHistoricalMapper.historicalDataList(topicHistoricalEntity);
            if (CollectionUtils.isEmpty(historicalDataEntities)) {
                return null;
            }
            if (topicHistoricalEntity.getBeginDate() == null || topicHistoricalEntity.getEndDate() == null) {
                throw new GovernanceException("beginDate or endDate is empty");
            }
            Date beginDate = topicHistoricalEntity.getBeginDate();
            Date endDate = topicHistoricalEntity.getEndDate();

            topicHistoricalEntity.setBeginDate(DateUtils.parseDate(DateFormatUtils.format(beginDate, simpleDateFormat), simpleDateFormat));
            topicHistoricalEntity.setEndDate(DateUtils.parseDate(DateFormatUtils.format(endDate, simpleDateFormat), simpleDateFormat));
            //deal data
            Map<String, List<TopicTopicHistoricalEntity>> map = new HashMap<>();
            historicalDataEntities.forEach(it -> {
                map.merge(it.getTopicName(), new ArrayList<>(Collections.singletonList(it)), this::mergeCollection);
            });
            List<String> listDate;
            listDate = listDate(topicHistoricalEntity.getBeginDate(), topicHistoricalEntity.getEndDate());

            map.forEach((k, v) -> {
                Map<String, Integer> eventCountMap = new HashMap<>();
                for (TopicTopicHistoricalEntity dataEntity : v) {
                    eventCountMap.put(DateFormatUtils.format(dataEntity.getCreateDate(), simpleDateFormat), dataEntity.getEventCount());
                }
                List<Integer> integerList = new ArrayList<>();
                for (String date : listDate) {
                    //Make sure there is data every day, even if it is zero
                    integerList.add(eventCountMap.get(date) == null ? 0 : eventCountMap.get(date));
                }
                returnMap.put(k, integerList);
            });
            return returnMap;
        } catch (Exception e) {
            log.info("get historicalDataEntity fail", e);
            throw new GovernanceException("get historicalDataEntity fail", e);
        }

    }

    public List<TopicTopicHistoricalEntity> eventList(TopicTopicHistoricalEntity topicHistoricalEntity, HttpServletRequest httpRequest) throws GovernanceException {
        try {
            String accountId = cookiesTools.getCookieValueByName(httpRequest, ConstantProperties.COOKIE_MGR_ACCOUNT_ID);
            Boolean flag = permissionService.verifyPermissions(topicHistoricalEntity.getBrokerId(), accountId);
            if (!flag) {
                throw new GovernanceException(ConstantCode.ACCESS_DENIED.getMsg());
            }
            List<TopicTopicHistoricalEntity> historicalEntities = topicHistoricalMapper.eventList(topicHistoricalEntity);
            if (CollectionUtils.isEmpty(historicalEntities)) {
                return historicalEntities;
            }
            for (TopicTopicHistoricalEntity entity : historicalEntities) {
                entity.setCreateDateStr(DateFormatUtils.format(entity.getCreateDate(), simpleDateFormat));
            }
            return historicalEntities;
        } catch (Exception e) {
            log.info("get eventList fail", e);
            throw new GovernanceException("get eventList fail", e);
        }

    }

    private List<TopicTopicHistoricalEntity> mergeCollection(List<TopicTopicHistoricalEntity> a, List<TopicTopicHistoricalEntity> b) {
        List<TopicTopicHistoricalEntity> list = new ArrayList<>();
        list.addAll(a);
        list.addAll(b);
        return list;
    }

    private List<String> listDate(Date beginDate, Date endDate) {
        List<String> dateList = new ArrayList<>();
        dateList.add(DateFormatUtils.format(beginDate, simpleDateFormat));
        Calendar calBegin = Calendar.getInstance();
        Calendar calEnd = Calendar.getInstance();
        calEnd.setTime(endDate);
        calBegin.setTime(beginDate);
        while (endDate.after(calBegin.getTime())) {
            calBegin.add(Calendar.DAY_OF_MONTH, 1);
            dateList.add(DateFormatUtils.format(calBegin.getTime(), simpleDateFormat));
        }
        return dateList;
    }

    @SuppressWarnings("unchecked")
    public void createRule(HttpServletRequest request, HttpServletResponse response, BrokerEntity brokerEntity) throws GovernanceException {
        String goalUrl = "";
        String user = "";
        String password = "";
        String dbName;
        try {
            Properties properties = new Properties();
            //get active files
            String activeFile = getActiveFile();
            //Read the configuration file to get the database information
            URL url = TopicHistoricalService.class.getClassLoader().getResource(activeFile);
            if (url != null) {
                properties.load(new FileInputStream(url.getFile()));
                goalUrl = properties.getProperty("spring.datasource.url");
                user = properties.getProperty("spring.datasource.username");
                password = properties.getProperty("spring.datasource.password");
            }
            // first use dbself database
            int first = goalUrl.lastIndexOf("/");
            int end = goalUrl.lastIndexOf("?");
            dbName = goalUrl.substring(first + 1, end);
            // get mysql default url like jdbc:mysql://127.0.0.1:3306
            String defaultUrl = goalUrl.substring(0, first);

            String dataBaseUrl = new StringBuffer(defaultUrl).append("/").append(dbName).append(ConstantProperties.QUESTION_MARK).append("user=").append(user)
                    .append(ConstantProperties.AND_SYMBOL).append("password=").append(password).toString();
            RuleDatabaseEntity ruleDatabaseEntity = initializationRuleDataBase(dbName + brokerEntity.getUserId(), TOPIC_HISTORICAL, dataBaseUrl, brokerEntity.getUserId());
            ruleDatabaseEntity = existRuleDataBase(ruleDatabaseEntity);
            if (ruleDatabaseEntity.getId() == null) {
                ruleDatabaseMapper.addCirculationDatabase(ruleDatabaseEntity);
            }

            //Request broker to get all groups
            List<String> groupList = getGroupList(request, brokerEntity);
            for (String groupId : groupList) {
                //get new tableName
                groupId = groupId.replaceAll("\"", "");
                RuleEngineEntity ruleEngineEntity = initializationRule("SYSTEM" + "-" + brokerEntity.getId() + "-" + groupId,
                        brokerEntity, groupId, ruleDatabaseEntity.getId());
                ruleEngineMapper.addRuleEngine(ruleEngineEntity);
                //built-in rule engine data and start
                ruleEngineService.startRuleEngine(ruleEngineEntity, request, response);
            }
        } catch (Exception e) {
            log.error("create rule fail error,{}", e.getMessage());
            throw new GovernanceException(e.getMessage());
        }
    }

    private RuleDatabaseEntity initializationRuleDataBase(String newDbName, String newTableName, String dataBaseUrl, Integer userId) {
        RuleDatabaseEntity ruleDatabaseEntity = new RuleDatabaseEntity();
        ruleDatabaseEntity.setDatabaseName(newDbName);
        ruleDatabaseEntity.setUserId(userId);
        ruleDatabaseEntity.setTableName(newTableName);
        ruleDatabaseEntity.setDatabaseUrl(dataBaseUrl);
        ruleDatabaseEntity.setIsVisible("2");
        return ruleDatabaseEntity;
    }

    private RuleEngineEntity initializationRule(String ruleName, BrokerEntity brokerEntity, String groupId, Integer dataBaseId) {
        String selectField = "eventId,topicName,groupId,brokerId";
        Map<String, String> map = new HashMap<>();
        map.put("topicName", "*");
        map.put("brokerId", "*");
        map.put("groupId", "*");
        map.put("eventId", "*");
        RuleEngineEntity ruleEngineEntity = new RuleEngineEntity();
        ruleEngineEntity.setRuleName(ruleName);
        ruleEngineEntity.setBrokerId(brokerEntity.getId());
        ruleEngineEntity.setStatus(StatusEnum.NOT_STARTED.getCode());
        ruleEngineEntity.setUserId(brokerEntity.getUserId());
        ruleEngineEntity.setGroupId(groupId);
        ruleEngineEntity.setCreateDate(new Date());
        ruleEngineEntity.setLastUpdate(new Date());
        ruleEngineEntity.setBrokerUrl(brokerEntity.getBrokerUrl() + "?groupId=" + groupId);
        ruleEngineEntity.setSelectField(selectField);
        ruleEngineEntity.setPayload(JSONObject.toJSONString(map));
        ruleEngineEntity.setRuleDataBaseId(dataBaseId);
        ruleEngineEntity.setPayloadType(PayloadEnum.JSON.getCode());
        ruleEngineEntity.setConditionType(ConditionTypeEnum.DATABASE.getCode());
        ruleEngineEntity.setFromDestination("#");
        ruleEngineEntity.setSystemTag("1");
        ruleEngineEntity.setOffSet("OFFSET_FIRST");
        return ruleEngineEntity;
    }

    private List getGroupList(HttpServletRequest request, BrokerEntity brokerEntity) throws GovernanceException {
        List groupList;
        String url = new StringBuffer(brokerEntity.getBrokerUrl()).append(ConstantProperties.REST_LIST_SUBSCRIPTION).toString();
        try {
            log.info("url:{}", url);
            CloseableHttpResponse closeResponse = commonService.getCloseResponse(request, url);
            String mes = EntityUtils.toString(closeResponse.getEntity());
            if (StringUtil.isBlank(mes)) {
                throw new GovernanceException("group is empty");
            }
            JSONObject jsonObject = JSONObject.parseObject(mes);
            Object data = jsonObject.get("data");
            if ("0".equals(jsonObject.get("code").toString()) && data instanceof List) {
                groupList = (List) data;
            } else {
                throw new GovernanceException(jsonObject.get("message").toString());
            }

            return groupList;
        } catch (Exception e) {
            log.error("get group list fail", e);
            throw new GovernanceException("get group list fail", e);
        }


    }

    private String getActiveFile() throws Exception {
        URL url = TopicHistoricalService.class.getClassLoader().getResource("application.properties");
        Properties properties = new Properties();
        properties.load(new FileInputStream(url.getFile()));
        String active = properties.getProperty("spring.profiles.active");
        return new StringBuffer("application").append("-").append(active).append(".properties").toString();
    }

    private RuleDatabaseEntity existRuleDataBase(RuleDatabaseEntity ruleDatabaseEntity) {
        List<RuleDatabaseEntity> ruleDatabaseEntities = ruleDatabaseMapper.circulationDatabaseList(ruleDatabaseEntity);
        if (CollectionUtils.isEmpty(ruleDatabaseEntities)) {
            return ruleDatabaseEntity;
        }
        return ruleDatabaseEntities.get(0);
    }


}
