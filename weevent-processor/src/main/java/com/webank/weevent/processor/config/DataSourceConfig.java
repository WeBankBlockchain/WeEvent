package com.webank.weevent.processor.config;

import java.io.FileInputStream;
import java.sql.Connection;
import java.util.Properties;

import javax.sql.DataSource;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbcp2.BasicDataSourceFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Slf4j
@Configuration
public class DataSourceConfig {
    private static DataSource ds;

    static {
        try {
            Properties applicationProperties = getProperties("application.properties");
            String currentApplication = "";
            if ("dev".equals(applicationProperties.get("spring.profiles.active"))) {
                currentApplication = "application-dev.properties";
            } else {
                currentApplication = "application-prod.properties";
            }
            Properties currentApplicationProperties = getProperties(currentApplication);

            ds = BasicDataSourceFactory.createDataSource(currentApplicationProperties);
            ((BasicDataSource) ds).setDriverClassName(currentApplicationProperties.get("spring.datasource.driverClassName").toString());
            ((BasicDataSource) ds).setUrl(currentApplicationProperties.get("spring.datasource.url").toString());
            ((BasicDataSource) ds).setPassword(currentApplicationProperties.get("spring.datasource.password").toString());
            ((BasicDataSource) ds).setUsername(currentApplicationProperties.get("spring.datasource.username").toString());

            log.info("create ds:{}", ds);
        } catch (Exception e) {
            log.info("exception:", e.toString());
        }
    }

    @Bean
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource")
    public static DataSource getDs() {
        return ds;
    }

    public static Connection getCon() {
        Connection con = null;
        try {
            con = ds.getConnection();
            log.info("get DataSource connection:{}", con);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return con;
    }

    private static Properties getProperties(String propertiesName) {
        try {
            String file = DataSourceConfig.class.getClassLoader().getResource(propertiesName).getPath();
            FileInputStream stream = new FileInputStream(file);
            Properties properties = new Properties();
            properties.load(stream);
            return properties;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
