package com.webank.weevent.governance.entity.base;

import java.util.List;

import javax.validation.constraints.Min;

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
public class BrokerBase extends BaseEntity {

    @Min(1)
    private Integer userId;

    private String name;

    @URL
    private String brokerUrl;

    private String webaseUrl;

    private Integer isDelete;

    /**
     * 1 means configured ,2 means not configured
     */
    private String isConfigRule;


}
