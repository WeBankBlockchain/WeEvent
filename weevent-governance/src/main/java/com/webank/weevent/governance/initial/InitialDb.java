package com.webank.weevent.governance.initial;

import java.io.FileInputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Map;
import org.yaml.snakeyaml.Yaml;

/**
 * tool to initdb
 */
public class InitialDb{
   
    public static void main(String[] args) throws Exception  
    {  
        String goalUrl = "";
        String user = "";
        String password = "";
        String driverName = "";
        try {
            Yaml yaml = new Yaml();
            URL url= InitialDb.class.getClassLoader().getResource("application-prod.yml");
            if(url != null) {
                Map map =(Map)yaml.load(new FileInputStream(url.getFile()));
                Map springMap = (Map) map.get("spring");
                Map dataSourceMap = (Map) springMap.get("datasource");
                goalUrl = (String) dataSourceMap.get("url");
                user = (String) dataSourceMap.get("username");
                password = (String) dataSourceMap.get("password");
                driverName = (String) dataSourceMap.get("driver-class-name");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        
        //first use dbself database
        int first = goalUrl.lastIndexOf("/");
        int end = goalUrl.lastIndexOf("?");
        String dbName = goalUrl.substring(first+1, end);
        String defaultUrl = goalUrl.replace(dbName, "mysql");
        
        Class.forName(driverName);
        Connection conn = DriverManager.getConnection(defaultUrl, user, password);  
        Statement stat = conn.createStatement();  
           
        //create database   
        stat.executeUpdate("create database if not exists "+ dbName);  
           
        //open database 
        stat.close();  
        conn.close();  
        conn = DriverManager.getConnection(goalUrl, user, password);    
        stat = conn.createStatement();  
           
        //create table topic_info  
        stat.executeUpdate("create table if not exists topic_info (topic_name varchar(255) not null primary key,creater varchar(255))engine =Innodb default charset=utf8");  
           
        stat.close();  
        conn.close();  
    }
}
