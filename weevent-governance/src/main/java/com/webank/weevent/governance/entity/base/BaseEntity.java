package com.webank.weevent.governance.entity.base;

import java.util.Date;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * BaseEntity class
 *
 * @since 2019/10/15
 */
@Data
@ToString
@EqualsAndHashCode(callSuper = false)
public class BaseEntity {

    /**
     * primary key
     */
    private Integer id;

    private Date createDate;

    private Date lastUpdate;
}
