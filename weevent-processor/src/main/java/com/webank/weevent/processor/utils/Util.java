package com.webank.weevent.processor.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class Util {


    /**
     * check the database url
     *
     * @param databaseUrl
     * @return
     */

    public static Connection getConnection(String databaseUrl) {
        String driver = "com.mysql.jdbc.Driver";
        try {
            Class.forName(driver);
            Connection conn = DriverManager.getConnection(databaseUrl);
            return conn;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<String> getKeys(String objJson) {
        List<String> keys = new ArrayList<>();
        try {
            Iterator<String> sIterator = (new org.json.JSONObject(objJson)).keys();

            while (sIterator.hasNext()) {
                String key = (sIterator.next());
                keys.add(key);
                System.out.println("key:.... " + key);

            }
        } catch (org.json.JSONException e) {
            e.printStackTrace();
        }
        return keys;
    }
}



