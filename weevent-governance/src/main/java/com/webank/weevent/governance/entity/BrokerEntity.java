package com.webank.weevent.governance.entity;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

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
@Table(name = "t_broker")
public class BrokerEntity extends BrokerBase {


    @Transient
    private List<Integer> userIdList;

    /**
     * 1 means creator, 2 means is authorized
     */
    @Transient
    private String isCreator;
}
