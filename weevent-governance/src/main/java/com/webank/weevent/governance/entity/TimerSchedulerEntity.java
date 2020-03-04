package com.webank.weevent.governance.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.webank.weevent.governance.entity.base.TimerSchedulerBase;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@EqualsAndHashCode(callSuper = true)
@Setter
@Getter
@Entity
@Table(name = "t_timer_scheduler")
public class TimerSchedulerEntity extends TimerSchedulerBase {

    @Transient
    private Integer totalCount;

    @Transient
    private Integer pageSize;

    @Transient
    private Integer pageNumber;

    @Transient
    private String databaseUrl;

    @Transient
    private String dataBaseType;


}
