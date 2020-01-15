package com.webank.weevent.processor.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public class TimerSchedulerBase {

    /**
     * primary key
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "scheduler_name")
    private String schedulerName;

    @Column(name = "jdbc_url")
    private String jdbcUrl;

    @Column(name = "period_params")
    private String periodParams;

    @Column(name = "delay_time")
    private Long delayTime;

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


    public Long getDelayTime() {
        return delayTime;
    }

    public void setDelayTime(Long delayTime) {
        this.delayTime = delayTime;
    }

    public String getPeriodParams() {
        return periodParams;
    }

    public void setPeriodParams(String periodParams) {
        this.periodParams = periodParams;
    }

    public TimerSchedulerBase(String schedulerName, String jdbcUrl, String periodParams, Long delayTime, String parsingSql) {
        this.schedulerName = schedulerName;
        this.jdbcUrl = jdbcUrl;
        this.periodParams = periodParams;
        this.delayTime = delayTime;
        this.parsingSql = parsingSql;
    }

    public TimerSchedulerBase() {
    }
}
