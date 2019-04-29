package com.webank.weevent.governance.configuration;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;
import org.influxdb.dto.Point.Builder;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;

public class InfluxDBConnect {

    private String enabled;
    private String username;
    private String password;
    private String openurl;
    private String database;

    private InfluxDB influxDB;

    public InfluxDBConnect(
        String enabled,
        String username,
        String password,
        String openurl,
        String database) {
        this.enabled = enabled;
        this.username = username;
        this.password = password;
        this.openurl = openurl;
        this.database = database;
    }

    /** concect database get InfluxDB **/
    public InfluxDB influxDbBuild() {
        if (influxDB == null) {
            influxDB = InfluxDBFactory.connect(openurl, username, password);

        }
        return influxDB;
    }

    /**
     * createRetentionPolicy
     */
    public void createRetentionPolicy() {
        String command = String.format(
            "CREATE RETENTION POLICY \"%s\" ON \"%s\" DURATION %s REPLICATION %s DEFAULT",
            "defalut",
            database,
            "30d",
            1);
        this.query(command);
    }

    /**
     * select
     * 
     * @param select command
     * @return
     */
    public QueryResult query(String command) {
        return influxDB.query(new Query(command, database));
    }

    /**
     * insert
     * 
     * @param measurement
     * @param tags
     * @param fields
     */
    public void insert(String measurement, Map<String, String> tags, Map<String, Object> fields) {
        Builder builder = Point.measurement(measurement);
        builder.time(((long) fields.get("currentTime")) * 1000000, TimeUnit.NANOSECONDS);
        builder.tag(tags);
        builder.fields(fields);
        influxDB.write(database, "", builder.build());
    }

    /**
     * deltet
     * 
     * @param command
     * @return errormessage
     */
    public String deleteMeasurementData(String command) {
        QueryResult result = influxDB.query(new Query(command, database));
        return result.getError();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getOpenurl() {
        return openurl;
    }

    public void setOpenurl(String openurl) {
        this.openurl = openurl;
    }

    public void setDatabase(String database) {
        this.database = database;
    }
    
    public String getEnabled() {
        return enabled;
    }

    public void setEnabled(String enabled) {
        this.enabled = enabled;
    }
}
