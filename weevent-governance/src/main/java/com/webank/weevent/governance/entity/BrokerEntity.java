package com.webank.weevent.governance.entity;

import com.webank.weevent.governance.entity.base.BrokerBase;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import java.util.List;

/**
 * BrokerEntity class
 *
 * @since 2019/04/28
 */
@Setter
@Getter
@EqualsAndHashCode(callSuper = false)
@ToString
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

    @Transient
    private List<Integer> ruleIdList;
}
