package com.webank.weevent.governance.entity;

import java.util.List;

import javax.validation.constraints.Min;

import com.webank.weevent.governance.entity.base.BaseEntity;

import lombok.Data;
import lombok.ToString;
import org.hibernate.validator.constraints.URL;

/**
 * BrokerEntity class
 *
 * @since 2019/04/28
 */
@Data
@ToString
public class BrokerEntity extends BaseEntity {

    @Min(1)
    private Integer userId;

    private String name;

    @URL
    private String brokerUrl;

    @URL
    private String webaseUrl;

    private List<Integer> userIdList;

    private Integer isDelete;



}
