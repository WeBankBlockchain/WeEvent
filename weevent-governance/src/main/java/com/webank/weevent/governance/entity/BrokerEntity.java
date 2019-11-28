package com.webank.weevent.governance.entity;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import com.webank.weevent.governance.entity.base.BrokerBase;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * BrokerEntity class
 *
 * @since 2019/04/28
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "t_broker",
        uniqueConstraints = {@UniqueConstraint(name = "brokerUrlDeleteAt",
                                                columnNames = {"broker_url", "delete_at"})})
public class BrokerEntity extends BrokerBase {

    @Transient
    private List<Integer> userIdList;

    /**
     * 1 means creator, 2 means is authorized
     */
    @Transient
    private String isCreator;
}
