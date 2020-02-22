package com.webank.weevent.governance.common;

import lombok.Data;
import org.springframework.stereotype.Component;

/**
 * constants.
 */
@Data
@Component
public class ConstantProperties {

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


    //project business constant==============================
    public final static Integer RULE_DESTINATION_TOPIC = 1;
    public final static Integer RULE_DESTINATION_DATABASE = 2;

    public final static long NOT_DELETED = 0L;


    public final static String CREATOR = "1";
    public final static String AUTHORIZED = "2";

    public final static Integer JSON = 1;

    public final static Integer NOT_STARTED = 0;
    public final static Integer RUNNING = 1;
    public final static Integer IS_DELETED = 2;

    //project business constant==============================


    // databaseType================
    public final static String H2_DATABASE = "1";
    public final static String MYSQL_DATABASE = "2";
    // databaseType ==============

    // checkType================
    public final static String CHECK_DATABASE = "1";
    public final static String CHECK_TABLE = "2";
    // checkType ==============


}