package com.webank.weevent.governance.initial;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import lombok.extern.slf4j.Slf4j;

/**
 * tool to initdb
 */
@Slf4j
public class InitialDb {

    public static void main(String[] args) throws Exception {
        String goalUrl = "";
        String user = "";
        String password = "";
        String driverName = "";
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
        int first = goalUrl.lastIndexOf("/");
        int end = goalUrl.lastIndexOf("?");
        String dbName = goalUrl.substring(first + 1, end);
        // get mysql default url like jdbc:mysql://127.0.0.1:3306
        String defaultUrl = goalUrl.substring(0, first);
        Class.forName(driverName);

        try (Connection conn = DriverManager.getConnection(defaultUrl, user, password);
             Statement stat = conn.createStatement()) {
            String sql = "create database if not exists  " + dbName;
            stat.executeUpdate(sql);
        } catch (SQLException e) {
            log.error(e.getMessage());
            throw e;
        }

        try (Connection conn = DriverManager.getConnection(goalUrl, user, password);
             Statement stat = conn.createStatement()) {
            // create table
            List<String> tableSqlList = readSql();
            for (String sql : tableSqlList) {
                stat.executeUpdate(sql);
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
            throw e;
        }
    }

    private static List<String> readSql() throws IOException {
        InputStream resourceAsStream = InitialDb.class.getResourceAsStream("/script/governance.sql");//配置文件路径
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
            if (!sql.equals("")) {
                sqlList.add(sql);
            }
            resourceAsStream.close();
        }
        return sqlList;
    }
}
