package com.webank.weevent.governance.entity.base;

import java.util.Date;

import lombok.Data;

@Data
public class BaseEntity {

    /**
     * primary key
     */
    private Integer id;

    private Date createDate;

    private Date lastUpdate;
}
