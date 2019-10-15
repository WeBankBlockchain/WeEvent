package com.webank.weevent.governance.entity;

import java.util.List;

import javax.validation.constraints.Min;

import com.webank.weevent.governance.entity.base.BaseEntity;
import com.webank.weevent.governance.entity.base.BrokerBase;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.URL;

/**
 * BrokerEntity class
 *
 * @since 2019/04/28
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class BrokerEntity extends BrokerBase {

    private List<Integer> userIdList;

    /**
     * 1 means creator, 2 means is authorized
     */
    private String isCreator;


}
