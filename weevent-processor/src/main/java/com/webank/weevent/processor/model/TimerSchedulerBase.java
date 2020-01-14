package com.webank.weevent.processor.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public class TimerSchedulerBase {

    /**
     * primary key
     */
    @Column(name = "id")
    private Integer id;

    @Column(name = "scheduler_name")
    private String schedulerName;

    @Column(name = "jdbc_url")
    private String jdbcUrl;

    @Column(name = "time_period")
    private Long timePeriod;

    @Column(name = "parsing_sql")
    private String parsingSql;

    @Column(name = "created_time")
    private Date createdTime;


    @Column(name = "updated_time")
    private Date updatedTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getSchedulerName() {
        return schedulerName == null ? null : schedulerName.trim();
    }

    public void setSchedulerName(String schedulerName) {
        this.schedulerName = schedulerName;
    }

    public String getJdbcUrl() {
        return jdbcUrl == null ? null : jdbcUrl.trim();
    }

    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public Long getTimePeriod() {
        return timePeriod;
    }

    public void setTimePeriod(Long timePeriod) {
        this.timePeriod = timePeriod;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public Date getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(Date updatedTime) {
        this.updatedTime = updatedTime;
    }

    public String getParsingSql() {
        return parsingSql;
    }

    public void setParsingSql(String parsingSql) {
        this.parsingSql = parsingSql;
    }

    public TimerSchedulerBase(String schedulerName, String jdbcUrl, Long timePeriod, String parsingSql, Date createdTime, Date updatedTime) {
        this.schedulerName = schedulerName;
        this.jdbcUrl = jdbcUrl;
        this.timePeriod = timePeriod;
        this.parsingSql = parsingSql;
        this.createdTime = createdTime;
        this.updatedTime = updatedTime;
    }



    public TimerSchedulerBase() {
    }
}
