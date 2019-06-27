package com.webank.weevent.governance.properties;

import java.math.BigInteger;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * constants.
 */
@Data
@Component
@ConfigurationProperties(prefix = ConstantProperties.CONSTANT_PREFIX)
public class ConstantProperties {

    // constant
    public static final String CONSTANT_PREFIX = "constant";
    public static final String COOKIE_JSESSIONID = "JSESSIONID"; // cookie key---session
    public static final String COOKIE_MGR_ACCOUNT = "GOVERNANCE_MGR_ACCOUNT_C"; // cookie key---account
    public static final String COOKIE_MGR_ACCOUNT_ID = "GOVERNANCE_MGR_ACCOUNT_ID";
    public static final String SESSION_MGR_ACCOUNT = "GOVERNANCE_MGR_ACCOUNT_S"; // session key---account

    private Integer maxRequestFail = 3;
    private Long sleepWhenHttpMaxFail = 60000L; // default 1min

    // COOKIE
    private Integer cookieMaxAge = 24 * 60 * 60; // seconds

}