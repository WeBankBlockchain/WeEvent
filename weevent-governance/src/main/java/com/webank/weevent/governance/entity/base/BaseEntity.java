package com.webank.weevent.governance.entity.base;

import java.util.Date;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class BaseEntity {

    /**
     * primary key
     */
    private Integer id;

    private Date createDate;

    private Date lastUpdate;
}
