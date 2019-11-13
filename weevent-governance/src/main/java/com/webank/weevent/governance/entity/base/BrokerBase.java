package com.webank.weevent.governance.entity.base;

import javax.validation.constraints.Min;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.URL;

/**
 * BrokerBase class
 *
 * @since 2019/10/15
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

    public void setBrokerUrl(String brokerUrl) {
        this.brokerUrl = brokerUrl == null ? null : brokerUrl.trim();
    }


}
