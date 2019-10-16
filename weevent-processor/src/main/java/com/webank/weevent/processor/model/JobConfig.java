package com.webank.weevent.processor.model;

import java.util.Date;



public class JobConfig {
    private String id;

    public void setId(String id) {
        this.id = id;
    }

    private Date createAt;

    private String cronTime;

    private String fullEntity;

    private String groupName;

    private String name;

    private Integer status;

    private String updateAt;

    public JobConfig(String id, Date createAt, String cronTime, String fullEntity, String groupName, String name, Integer status, String updateAt) {
        this.id = id;
        this.createAt = createAt;
        this.cronTime = cronTime;
        this.fullEntity = fullEntity;
        this.groupName = groupName;
        this.name = name;
        this.status = status;
        this.updateAt = updateAt;
    }

    public JobConfig() {
        super();
    }

    public String getId() {
        return id;
    }


    public Date getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Date createAt) {
        this.createAt = createAt;
    }

    public String getCronTime() {
        return cronTime;
    }

    public void setCronTime(String cronTime) {
        this.cronTime = cronTime == null ? null : cronTime.trim();
    }

    public String getFullEntity() {
        return fullEntity;
    }

    public void setFullEntity(String fullEntity) {
        this.fullEntity = fullEntity == null ? null : fullEntity.trim();
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName == null ? null : groupName.trim();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getUpdateAt() {
        return updateAt;
    }

    public void setUpdateAt(String updateAt) {
        this.updateAt = updateAt;
    }

}
