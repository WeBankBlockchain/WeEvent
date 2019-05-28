package com.webank.weevent.governance.code;

/**
 * ConstantCode class
 * 
 * @since 2019/05/23
 */
public class ConstantCode {

    /* return success */
    public static final RetCode SUCCESS = RetCode.mark(0, "success");

    public static final RetCode LOGIN_FAIL = RetCode.mark(202034, "login fail");

    /* auth */
    public static final RetCode USER_NOT_LOGGED_IN = RetCode.mark(302000, "user not logged in");
    public static final RetCode ACCESS_DENIED = RetCode.mark(302001, "access denied");

    /* param exception */
    public static final RetCode PARAM_EXCEPTION = RetCode.mark(402000, "param exception");

}
