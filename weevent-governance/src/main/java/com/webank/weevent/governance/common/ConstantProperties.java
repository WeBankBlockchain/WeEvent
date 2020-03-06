package com.webank.weevent.governance.common;

/**
 * constants.
 */
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


    //process rule url begin ========================================================
    public final static String PROCESSOR_INSERT = "/insert";
    public final static String PROCESSOR_DELETE_CEP_RULE = "/deleteCEPRuleById";
    public final static String PROCESSOR_UPDATE_CEP_RULE = "/updateCEPRuleById";
    public final static String PROCESSOR_CHECK_WHERE_CONDITION = "/checkWhereCondition";
    public final static String PROCESSOR_START_CEP_RULE = "/startCEPRule";
    public final static String PROCESSOR_STOP_CEP_RULE = "/stopCEPRuleById";


    //process rule url end ===========================================================

    //process timerScheduler url begin ===========================================================

    public final static String TIMER_SCHEDULER_INSERT = "/timerScheduler/insert";
    public final static String TIMER_SCHEDULER_UPDATE = "/timerScheduler/update";
    public final static String TIMER_SCHEDULER_DELETE = "/timerScheduler/delete";

    //process timerScheduler url end ===========================================================


    //weBase url begin ========================================================

    public final static String WEBASE_NODE_URL = "/node/nodeInfo/1";

    //weBase url end ===========================================================
}