package com.webank.weevent.governance.entity;

import java.util.Date;

import lombok.Data;
import lombok.ToString;

/**
 * Broker class
 *
 * @since 2019/04/28
 */
@Data
@ToString
public class Broker {

	private Integer id;
	private Integer userId;
	private String name;
	private String brokerUrl;
	private Date lastUpdate;
}
