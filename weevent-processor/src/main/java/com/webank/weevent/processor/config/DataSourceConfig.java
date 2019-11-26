package com.webank.weevent.processor.config;

import javax.sql.DataSource;

import com.webank.weevent.processor.ProcessorApplication;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class DataSourceConfig {

    @Bean
    @Primary
    @ConfigurationProperties(prefix="spring.datasource")
    public DataSource getDataSource() {
        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.driverClassName(ProcessorApplication.processorConfig.getDataSourceDriver());
        dataSourceBuilder.url(ProcessorApplication.processorConfig.getDataSourceUrl());
        dataSourceBuilder.username(ProcessorApplication.processorConfig.getDataSourceUser());
        dataSourceBuilder.password(ProcessorApplication.processorConfig.getDataSourcePassword());
        return dataSourceBuilder.build();
    }
}
