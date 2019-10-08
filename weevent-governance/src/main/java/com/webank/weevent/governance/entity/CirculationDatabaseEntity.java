package com.webank.weevent.governance.entity;

import com.webank.weevent.governance.entity.base.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * BrokerEntity class
 *
 * @since 2019/04/28
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class CirculationDatabaseEntity extends BaseEntity {

    private Integer userId;

    private Integer brokerId;

    private String databaseUrl;

}
