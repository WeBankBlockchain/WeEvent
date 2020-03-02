package com.webank.weevent.governance.entity;

import com.webank.weevent.governance.entity.base.TimerSchedulerBase;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

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
