package com.webank.weevent.governance.properties;

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
    public static final String COOKIE_GROUP_ID = "GOVERNANCE_GROUP_ID";
    public static final String SESSION_MGR_ACCOUNT = "GOVERNANCE_MGR_ACCOUNT_S"; // session key---account

    private static final String QUESTION_MARK = "?";
    private static final String AND_SYMBOL = "&";
    private static final String LAYER_SEPARATE = "/";

    //broker url begin ========================================================
    private final static String BROKER_LIST_URL = "/admin/getVersion";
    private final static String BROKER_REST_CLOSE = "/rest/close";
    private final static String BROKER_REST_LIST = "/rest//list";
    private final static String BROKER_REST_STATE = "/rest/state";
    private final static String BROKER_REST_OPEN = "/rest/open";

    private static final String BROKER_TRANS_DAILY = "/group/transDaily";
    private static final String BROKER_GROUP_GENERAL = "/group/general";
    private static final String BROKER_TRANS_LIST = "/transaction/transList";
    private static final String BROKER_BLOCK_LIST = "/block/blockList";
    private static final String BROKER_NODE_LIST = "/node/nodeList";


    //broker url end ===========================================================


    //process url begin ========================================================
    private final static String PROCESSOR_DELETE_CEP_RULE = "/processor/deleteCEPRuleById";
    private final static String PROCESSOR_UPDATE_CEP_RULE = "/processor/updateCEPRuleById";
    private final static String PROCESSOR_START_CEP_RULE = "/processor/startCEPRuleById";


    //process url end ===========================================================


    //weBase url begin ========================================================

    private final static String WEBASE_NODE_URL = "/node/nodeInfo/1";


    //weBase url end ===========================================================

    private Integer maxRequestFail = 3;
    private Long sleepWhenHttpMaxFail = 60000L; // default 1min

    // COOKIE
    private Integer cookieMaxAge = 24 * 60 * 60; // seconds

}