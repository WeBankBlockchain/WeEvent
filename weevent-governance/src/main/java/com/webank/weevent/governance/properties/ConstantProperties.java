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
    public static final String COOKIE_MGR_ACCOUNT_ID = "GOVERNANCE_MGR_ACCOUNT_ID";//cookie---account id
    public static final String COOKIE_GROUP_ID = "GOVERNANCE_GROUP_ID";//cookie---group id
    public static final String SESSION_MGR_ACCOUNT = "GOVERNANCE_MGR_ACCOUNT_S"; // session key---account

    public static final String QUESTION_MARK = "?";
    public static final String AND_SYMBOL = "&";
    public static final String LAYER_SEPARATE = "/";
    public static final String RIGHT_SLASH = "\"";
    public static final String ASTERISK = "*";
    public static final String EQUAL_SIGN = "=";
    public static final String LOWER_CONNECTOR = "_";
    //broker url begin ========================================================
    public final static String BROKER_LIST_URL = "/admin/getVersion";
    public final static String BROKER_REST_CLOSE = "/rest/close";
    public final static String BROKER_REST_LIST = "/rest/list";
    public final static String BROKER_REST_STATE = "/rest/state";
    public final static String BROKER_REST_OPEN = "/rest/open";
    public final static String BROKER_REST_EXIST = "/rest/exist";

    public static final String BROKER_TRANS_DAILY = "/group/transDaily";
    public static final String BROKER_GROUP_GENERAL = "/group/general";
    public static final String BROKER_TRANS_LIST = "/transaction/transList";
    public static final String BROKER_BLOCK_LIST = "/block/blockList";
    public static final String BROKER_NODE_LIST = "/node/nodeList";
    public static final String REST_LIST_SUBSCRIPTION = "/admin/listGroup";


    //broker url end ===========================================================


    //process url begin ========================================================
    public final static String PROCESSOR_INSERT = "/processor/insert";
    public final static String PROCESSOR_DELETE_CEP_RULE = "/processor/deleteCEPRuleById";
    public final static String PROCESSOR_UPDATE_CEP_RULE = "/processor/updateCEPRuleById";
    public final static String PROCESSOR_CHECK_WHERE_CONDITION = "/processor/checkWhereCondition";
    public final static String PROCESSOR_START_CEP_RULE = "/processor/startCEPRule";
    public final static String PROCESSOR_STOP_CEP_RULE = "/processor/stopCEPRuleById";


    //process url end ===========================================================


    //weBase url begin ========================================================

    public final static String WEBASE_NODE_URL = "/node/nodeInfo/1";


    //weBase url end ===========================================================

    public Integer maxRequestFail = 3;
    public Long sleepWhenHttpMaxFail = 60000L; // default 1min

    // COOKIE
    public Integer cookieMaxAge = 86400; //  24 * 60 * 60 seconds

}