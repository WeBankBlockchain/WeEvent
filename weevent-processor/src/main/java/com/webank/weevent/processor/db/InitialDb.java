package com.webank.weevent.processor.db;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.webank.weevent.sdk.BrokerException;

import lombok.extern.slf4j.Slf4j;

/**
 * init db tool
 */
@Slf4j
public class InitialDb implements AutoCloseable {

    public static void main(String[] args) throws Exception {
        String goalUrl = "";
        String user = "";
        String password = "";
        String driverName = "";
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
            }
        } catch (Exception e) {
            log.error("read database properties error,{}", e.getMessage());
        }

        // first use dbself database
        int first = goalUrl.lastIndexOf("/") + 1;
        dbName = goalUrl.substring(first);
        // get mysql default url like jdbc:mysql://127.0.0.1:3306
        String defaultUrl = goalUrl.substring(0, first - 1);
        Class.forName(driverName);

        List<String> tableSqlList = readCEPSql();
        try (Connection conn = DriverManager.getConnection(defaultUrl, user, password);
             Statement stat = conn.createStatement()) {
            String querySql = "SELECT count(1) FROM information_schema.SCHEMATA where SCHEMA_NAME=" + "'" + dbName + "'";
            ResultSet resultSet = stat.executeQuery(querySql);
            while (resultSet.next()) {
                int num = resultSet.getInt(1);
                if (num == 1) {
                    log.error("database {} {}", dbName, " is exist!");
                    throw new BrokerException("database " + dbName + " is exist!");
                }
            }
            String dbSql = "create database " + dbName + " default character set utf8 collate utf8_general_ci;";
            tableSqlList.add(0, dbSql);
            String useDataBase = "use " + dbName + ";";
            tableSqlList.add(1, useDataBase);
            for (String sql : tableSqlList) {
                stat.executeUpdate(sql);
            }
            log.info("create database {} {}", dbName, " success!");
        } catch (Exception e) {
            log.error("create database fail,message: {}", e.getMessage());
            throw new BrokerException(e.getMessage());
        }
    }

    private static List<String> readCEPSql() throws IOException {
        InputStream resourceAsStream = InitialDb.class.getResourceAsStream("/cep_rule.sql");
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
        log.info("resource is close");
    }
}
