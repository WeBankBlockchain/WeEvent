package com.webank.weevent.processor.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import com.webank.weevent.processor.model.CEPRule;
import com.webank.weevent.processor.utils.Constants;
import com.webank.weevent.sdk.BrokerException;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public class AnalysisWeEventIdService implements AutoCloseable {

    public static void analysisWeEventId(CEPRule rule, String eventId) throws BrokerException {
        if (StringUtils.isBlank(eventId)) {
            throw new BrokerException("eventId is empty");
        }
        String dataBaseUrl = rule.getDatabaseUrl();
        if (StringUtils.isBlank(dataBaseUrl)) {
            return;
        }
        //get database username password
        String defaultUrl = dataBaseUrl.substring(0, dataBaseUrl.indexOf(Constants.QUESTION_MARK));
        String user = dataBaseUrl.substring(dataBaseUrl.indexOf(Constants.QUESTION_MARK) + 1, dataBaseUrl.indexOf(Constants.AND_SYMBOL));
        user = user.split(Constants.QUALS_TO)[1].replaceAll("\"", "");
        int first = dataBaseUrl.indexOf(Constants.AND_SYMBOL);
        int second = dataBaseUrl.indexOf(Constants.AND_SYMBOL, first + 1);
        String password = dataBaseUrl.substring(first, second);
        password = password.split(Constants.QUALS_TO)[1].replaceAll("\"", "");
        try (Connection conn = DriverManager.getConnection(defaultUrl, user, password)) {
            if (conn == null) {
                log.info("database connect fail,dataBaseUrl:{}", defaultUrl);
                throw new BrokerException("database connect fail,dataBaseUrl:" + defaultUrl);
            }
            Statement statement = conn.createStatement();
            String insertSql = getInsertSql(eventId, rule);
            statement.execute(insertSql);
        } catch (Exception e) {
            log.error("analysis weEventId error", e);
            throw new BrokerException("analysis weEventId error", e);
        }
    }

    private static String getInsertSql(String eventId, CEPRule cepRule) {
        String brokerUrl = cepRule.getBrokerUrl();

        String[] split = eventId.split(Constants.CONNECTION_SYMBOL);
        String lastBlock = split[2];
        String[] brokerArray = brokerUrl.split(Constants.QUALS_TO);
        String groupId = brokerArray[1];
        String sql = new StringBuffer("insert into t_historical_data").append("(")
                .append("topic_name,group_id,block_number,user_id,broker_id,event_id")
                .append(")").append("values(").append("\"")
                .append(cepRule.getFromDestination()).append("\",")
                .append("\"").append(groupId).append("\",")
                .append(lastBlock).append(",")
                .append(cepRule.getUserId()).append(",")
                .append(cepRule.getBrokerId()).append(",")
                .append("\"").append(eventId).append("\"")
                .append(")").toString();
        return sql;
    }

    @Override
    public void close() throws Exception {
        log.info("resource is close");
    }
}
