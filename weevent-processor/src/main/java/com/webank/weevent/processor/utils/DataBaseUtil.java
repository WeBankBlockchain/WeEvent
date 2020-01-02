package com.webank.weevent.processor.utils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.webank.weevent.processor.model.CEPRule;
import com.webank.weevent.sdk.WeEvent;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DataBaseUtil {

    public static String sendMessageToDB(WeEvent eventContent, CEPRule rule) {
        try {
            try (Connection conn = CommonUtil.getDbcpConnection(rule.getDatabaseUrl())) {

                if (conn != null) {
                    // get the insert sql
                    StringBuffer insertExpression = new StringBuffer("insert into ");
                    insertExpression.append(rule.getTableName());
                    insertExpression.append("(");
                    StringBuffer values = new StringBuffer(" values (");

                    // select key and value
                    Map<String, String> sqlvalue = CommonUtil.contactsql(rule, eventContent);

                    // just the order key and need write in db
                    List<String> keys = CommonUtil.getAllKey(sqlvalue);

                    // payload just like the table
                    for (int i = 0; i < keys.size(); i++) {
                        if ((keys.size() - 1) == i) {
                            // last key
                            insertExpression.append(keys.get(i)).append(")");
                            values.append("?)");
                        } else {
                            // concat the key
                            insertExpression.append(keys.get(i)).append(",");
                            values.append("?,");
                        }
                    }

                    StringBuffer query = insertExpression.append(values);
                    log.info("query:{}", query);
                    PreparedStatement preparedStmt = conn.prepareStatement(query.toString());
                    for (int t = 0; t < keys.size(); t++) {
                        preparedStmt.setString(t + 1, sqlvalue.get(keys.get(t)));
                    }
                    log.info("preparedStmt:{}", preparedStmt.toString());
                    // execute the prepared statement
                    int res = preparedStmt.executeUpdate();
                    preparedStmt.close();
                    conn.close();

                    if (res > 0) {
                        log.info("insert db success...");
                        return ConstantsHelper.WRITE_DB_SUCCESS;
                    } else {
                        return ConstantsHelper.WRITE_DB_FAIL;
                    }
                }
            }
        } catch (SQLException | IOException e) {
            //statisticWeEvent.getStatisticRuleMap().get(rule.getId()).setLastFailReason(e.toString());
            log.info(e.toString());
            return ConstantsHelper.LAST_FAIL_REASON;
        }
        return "";
    }
}
