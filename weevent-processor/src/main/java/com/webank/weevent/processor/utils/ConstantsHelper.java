package com.webank.weevent.processor.utils;

public class ConstantsHelper {
    public static RetCode SUCCESS = RetCode.mark(1, "success");
    public static RetCode FAIL = RetCode.mark(0, "fail");

    public static RetCode RET_SUCCESS = RetCode.mark(0, "success");
    public static RetCode RET_FAIL = RetCode.mark(1, "fail");

    public static RetCode RULENAME_IS_BLANK = RetCode.mark(280001, "rule name is blank");
    public static RetCode PAYLOAD_IS_BLANK = RetCode.mark(280002, "payload  is blank");
    public static RetCode ALREADY_DELETE = RetCode.mark(270003, "the rule already delete");
    public static RetCode URL_ISNOT_VALID = RetCode.mark(270004, "url is valid");
    public static RetCode INSERT_RECORD_FAIL = RetCode.mark(270005, "insert fail");
    public static RetCode TOPIC_ISNOT_EXIST = RetCode.mark(270006, "the topic is not exist");
    public static RetCode PAYLOAD_ISNOT_JSON = RetCode.mark(270007, "payload is not a json");
    public static RetCode CONDITIONTYPE_ISNOT_VALID = RetCode.mark(270008, "condition type is not valid");
    public static RetCode  RULE_IS_NOT_VALID = RetCode.mark(270009, "rule is not valid");
    public static RetCode BROKERID_IS_BLANK = RetCode.mark(280010, "broker id is blank");
    public static RetCode USERID_IS_BLANK = RetCode.mark(280011, "user id is blank");



    public static final String QUESTION_MARK = "?";
    public static final String AND_SYMBOL = "&";
    public static final String CONNECTION_SYMBOL = "-";
    public static final String QUALS_TO="=";
    public static final String NOT_QUALS_TO="<>";
    public static final String NOT_QUALS_TO_TWO="!=";
    public static final String MINOR_THAN="<";
    public static final String MINOR_THAN_EQUAL="<=";
    public static final String GREATER_THAN=">";
    public static final String GREATER_THAN_EQUAL=">=";
    public static final String BETWEEN="BETWEEN";
    public static final String LIKE="LIKE";
    public static final String IN="IN";
    public static final String AND="AND";
    public static final String OR="OR";



    public static final Integer RULE_STATUS_START = 1;
    public static final Integer RULE_STATUS_DELETE = 2;
    public static final Integer SUCCESS_CODE = 0;
    public static final String SUCCESS_MESSAGE = "success";


}
