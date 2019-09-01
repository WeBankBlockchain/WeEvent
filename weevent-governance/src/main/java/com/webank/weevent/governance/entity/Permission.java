package com.webank.weevent.governance.entity;

import java.util.Date;

import lombok.Data;
import lombok.ToString;

/**
 *
 * @author puremilkfan
 * @since 2019-08-28
 */
@Data
@ToString
public class Permission {

    /**
     * primary key
     */
    private Integer id;

    private Integer userId;

    private Date createDate;

    private Integer brokerId;

    private Date lastUpdate;

}
