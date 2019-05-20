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
                goalUrl = dataSourceMap.get("url").toString();
                user = dataSourceMap.get("username").toString();
                password = dataSourceMap.get("password").toString();
                driverName = dataSourceMap.get("driver-class-name").toString();
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        
        //first use dbself database
        int first = goalUrl.lastIndexOf("/");
        int end = goalUrl.lastIndexOf("?");
        String dbName = goalUrl.substring(first+1, end);
        //get mysql default url like jdbc:mysql://127.0.0.1:3306
        String defaultUrl = goalUrl.substring(0,first);
        
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
           
        //create table t_broker  
        stat.executeUpdate("create table if not exists t_broker(\n" + 
        		"id int(11) NOT NULL auto_increment primary key,\n" + 
        		"name varchar(256) not null,\n" + 
        		"broker_url varchar(256),\n" + 
        		"last_update timestamp NOT NULL , \n" + 
        		"key index_name (name)\n" + 
        		")engine =Innodb default charset=utf8;");  
        
        //create table t_topic
        stat.executeUpdate("create table if not exists t_topic(\n" + 
        		"topic_name varchar(256) not null,\n" + 
        		"creater varchar(256),\n" + 
        		"broker_id int(11) not null,\n" + 
        		"last_update timestamp NOT NULL , \n" + 
        		"foreign key(broker_id) references t_broker(id),\n" + 
        		"primary key id_topic (broker_id,topic_name)\n" + 
        		")engine =Innodb default charset=utf8");
           
        stat.close();  
        conn.close();  
    }
}
