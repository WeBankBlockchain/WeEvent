package com.webank.weevent.governance.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.springframework.beans.BeanUtils;
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

            TopicTopicHistoricalEntity historicalEntity = new TopicTopicHistoricalEntity();
            BeanUtils.copyProperties(topicHistoricalEntity, historicalEntity, "userId", "brokerId", "groupId");
            String tableName = new StringBuffer(TOPIC_HISTORICAL).append("_").append(topicHistoricalEntity.getBrokerId()).append("_").append(topicHistoricalEntity.getGroupId()).toString();
            historicalEntity.setTableName(tableName);
            List<TopicTopicHistoricalEntity> historicalDataEntities = topicHistoricalMapper.historicalDataList(historicalEntity);
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
            TopicTopicHistoricalEntity historicalEntity = new TopicTopicHistoricalEntity();
            BeanUtils.copyProperties(topicHistoricalEntity, historicalEntity, "userId", "brokerId", "groupId");
            String tableName = new StringBuffer(TOPIC_HISTORICAL).append("_").append(topicHistoricalEntity.getBrokerId()).append("_").append(topicHistoricalEntity.getGroupId()).toString();
            historicalEntity.setTableName(tableName);
            List<TopicTopicHistoricalEntity> historicalEntities = topicHistoricalMapper.eventList(historicalEntity);
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

    public void createTopicHistoricalTable(HttpServletRequest request, HttpServletResponse response, BrokerEntity brokerEntity) throws GovernanceException {
        String goalUrl = "";
        String user = "";
        String password = "";
        String driverName = "";
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
                driverName = properties.getProperty("spring.datasource.driver-class-name");
            }
            // first use dbself database
            int first = goalUrl.lastIndexOf("/");
            int end = goalUrl.lastIndexOf("?");
            dbName = goalUrl.substring(first + 1, end);
            // get mysql default url like jdbc:mysql://127.0.0.1:3306
            String defaultUrl = goalUrl.substring(0, first);
            Class.forName(driverName);
            //Request broker to get all groups
            List<String> groupList = getGroupList(request, brokerEntity);
            for (String groupId : groupList) {
                //get new tableName
                groupId = groupId.replaceAll("\"", "");
                String newTableName = TOPIC_HISTORICAL + "_" + brokerEntity.getId() + "_" + groupId;
                List<String> tableSqlList = readSql(TOPIC_HISTORICAL, newTableName, dbName);
                //create a table
                this.executeCreate(tableSqlList, defaultUrl, user, password);
                //built-in rule engine data and start
                String dataBaseUrl = new StringBuffer(defaultUrl).append("/").append("user=").append(user)
                        .append(ConstantProperties.AND_SYMBOL).append("password=").append(password).toString();
                String newDbName = new StringBuffer(dbName).append("_").append(brokerEntity.getId()).append("_").append(groupId).toString();

                RuleDatabaseEntity ruleDatabaseEntity = initializationRuleDataBase(newDbName, newTableName, dataBaseUrl, brokerEntity.getUserId());
                ruleDatabaseMapper.addCirculationDatabase(ruleDatabaseEntity);

                RuleEngineEntity ruleEngineEntity = initializationRule(newTableName, brokerEntity, groupId, ruleDatabaseEntity.getId());
                ruleEngineMapper.addRuleEngine(ruleEngineEntity);
                //determine if the processor's service is available
                boolean exist = ruleEngineService.checkProcessorExist(request);
                if (exist) {
                    ruleEngineService.startRuleEngine(ruleEngineEntity, request, response);
                }
            }
        } catch (Exception e) {
            log.error("create table fail error,{}", e.getMessage());
            throw new GovernanceException(e.getMessage());
        }
    }

    private RuleDatabaseEntity initializationRuleDataBase(String newDbName, String newTableName, String dataBaseUrl, Integer userId) {
        RuleDatabaseEntity ruleDatabaseEntity = new RuleDatabaseEntity();
        ruleDatabaseEntity.setDatabaseName(newDbName);
        ruleDatabaseEntity.setUserId(userId);
        ruleDatabaseEntity.setTableName(newTableName);
        ruleDatabaseEntity.setDatabaseUrl(dataBaseUrl);
        return ruleDatabaseEntity;
    }

    private RuleEngineEntity initializationRule(String newTableName, BrokerEntity brokerEntity, String groupId, Integer dataBaseId) {
        String selectField = "topic_name,eventId";
        Map<String, String> map = new HashMap<>();
        map.put("topic_name", "*");
        map.put("eventId", "*");
        RuleEngineEntity ruleEngineEntity = new RuleEngineEntity();
        ruleEngineEntity.setRuleName(newTableName);
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
        return ruleEngineEntity;
    }

    private List<String> getGroupList(HttpServletRequest request, BrokerEntity brokerEntity) throws GovernanceException {
        List<String> groupList;
        String url = new StringBuffer(brokerEntity.getBrokerUrl()).append(ConstantProperties.REST_LIST_SUBSCRIPTION).toString();
        try {
            log.info("url:{}", url);
            CloseableHttpResponse closeResponse = commonService.getCloseResponse(request, url);
            String mes = EntityUtils.toString(closeResponse.getEntity());
            if (StringUtil.isBlank(mes)) {
                throw new GovernanceException("group is empty");
            }
            String[] split = mes.replace("[", "").replace("]", "").split(",");
            groupList = Arrays.asList(split);
            return groupList;
        } catch (Exception e) {
            log.error("get group list fail", e);
            throw new GovernanceException("get group list fail", e);
        }


    }

    //执行sql
    private void executeCreate(List<String> tableSqlList, String defaultUrl, String user, String password) throws GovernanceException {
        try (
                Connection conn = DriverManager.getConnection(defaultUrl, user, password);
                Statement stat = conn.createStatement()) {
            for (String sql : tableSqlList) {
                stat.executeUpdate(sql);
            }
            log.info("create table success!");
        } catch (Exception e) {
            log.error("create table fail,message: {}", e.getMessage());
            throw new GovernanceException(e.getMessage());
        }
    }

    //解析sql替换表名
    private static List<String> readSql(String oldTableName, String newTableName, String dbName) throws IOException {
        InputStream resourceAsStream = TopicHistoricalService.class.getResourceAsStream("/script/topicHistorical.sql");
        StringBuffer sqlBuffer = new StringBuffer("use ").append(dbName).append("; \n ");
        sqlBuffer.append("drop table if exists ").append(oldTableName).append(";\n ");
        List<String> sqlList = new ArrayList<>();
        byte[] buff = new byte[1024];
        int byteRead = 0;
        while ((byteRead = resourceAsStream.read(buff)) != -1) {
            sqlBuffer.append(new String(buff, 0, byteRead, Charset.defaultCharset()));
        }
        String[] sqlArr = sqlBuffer.toString().split("(;\\s*\\r\\n)|(;\\s*\\n)");

        for (int i = 0; i < sqlArr.length; i++) {
            String sql = sqlArr[i].replaceAll("--.*", "").trim();
            if (!StringUtil.isBlank(sql)) {
                sql = sql.replaceAll(oldTableName, newTableName);
                sqlList.add(sql);
            }
            resourceAsStream.close();
        }
        return sqlList;
    }

    private String getActiveFile() throws Exception {
        URL url = TopicHistoricalService.class.getClassLoader().getResource("application.properties");
        Properties properties = new Properties();
        properties.load(new FileInputStream(url.getFile()));
        String active = properties.getProperty("spring.profiles.active");
        return new StringBuffer("application").append("-").append(active).append(".properties").toString();
    }

}
