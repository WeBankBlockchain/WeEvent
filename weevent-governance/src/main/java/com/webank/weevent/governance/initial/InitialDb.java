package com.webank.weevent.governance.initial;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.webank.weevent.governance.exception.GovernanceException;

import lombok.extern.slf4j.Slf4j;

/**
 * tool to initdb
 */
@Slf4j
public class InitialDb implements AutoCloseable {

    public static void main(String[] args) throws Exception {
        String goalUrl = "";
        String user = "";
        String password = "";
        String driverName = "";
        String databaseType = "";
        String dbName;
        try {
            Properties properties = new Properties();
            URL url = InitialDb.class.getClassLoader().getResource("application-prod.properties");
            if (url != null) {
                properties.load(new FileInputStream(url.getFile()));
                goalUrl = properties.getProperty("spring.datasource.url");
                user = properties.getProperty("spring.datasource.username");
                password = properties.getProperty("spring.datasource.password");
                driverName = properties.getProperty("spring.datasource.driver-class-name");
                databaseType = properties.getProperty("spring.jpa.database").toLowerCase();
            }
        } catch (Exception e) {
            log.error("read database properties error,{}", e.getMessage());
        }
        boolean flag = ("mysql").equals(databaseType);

        // first use dbself database
        int first = goalUrl.lastIndexOf("/");
        int end = goalUrl.lastIndexOf("?");
        dbName = flag ? goalUrl.substring(first + 1, end) : goalUrl.substring(first);
        // get mysql default url like jdbc:mysql://127.0.0.1:3306
        String defaultUrl = flag ? goalUrl.substring(0, first) : goalUrl;
        Class.forName(driverName);

        List<String> tableSqlList = readSql(databaseType);
        try (Connection conn = DriverManager.getConnection(defaultUrl, user, password);
             Statement stat = conn.createStatement()) {
            String h2QuerySql = "SELECT count(1) FROM information_schema.SCHEMATA where CATALOG_NAME=" + "'" + dbName + "'";
            String mysqlQuerySql = "SELECT count(1) FROM information_schema.SCHEMATA where SCHEMA_NAME=" + "'" + dbName + "'";
            String querySql = flag ? mysqlQuerySql : h2QuerySql;
            ResultSet resultSet = stat.executeQuery(querySql);
            while (resultSet.next()) {
                int num = resultSet.getInt(1);
                if (num >= 1) {
                    log.error("database {} {}", dbName, " is exist!");
                    throw new GovernanceException("database " + dbName + " is exist!");
                }
            }
            String dbSql = "create database " + dbName + ";";
            String useDataBase = "use " + dbName + ";";
            if (flag) {
                tableSqlList.add(0, dbSql);
                tableSqlList.add(1, useDataBase);
            }
            for (String sql : tableSqlList) {
                stat.executeUpdate(sql);
            }
            log.info("create database {} {}", dbName, " success!");
        } catch (SQLException e) {
            log.error("create database fail,message: {}", e.getMessage());
            throw e;
        }
    }

    private static List<String> readSql(String dataBaseType) throws IOException {
        InputStream resourceAsStream = InitialDb.class.getResourceAsStream("/script/governance_" + dataBaseType + ".sql");
        StringBuffer sqlBuffer = new StringBuffer();
        List<String> sqlList = new ArrayList<>();
        byte[] buff = new byte[1024];
        int byteRead = 0;
        while ((byteRead = resourceAsStream.read(buff)) != -1) {
            sqlBuffer.append(new String(buff, 0, byteRead, Charset.defaultCharset()));
        }
        String[] sqlArr = sqlBuffer.toString().split("(;\\s*\\r\\n)|(;\\s*\\n)");

        for (int i = 0; i < sqlArr.length; i++) {
            String sql = sqlArr[i].replaceAll("--.*", "").trim();
            if (!("").equals(sql)) {
                sqlList.add(sql);
            }
            resourceAsStream.close();
        }
        return sqlList;
    }

    @Override
    public void close() throws Exception {
        log.error("resource is close");
    }
}
