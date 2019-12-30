package com.webank.weevent.governance.common;

/**
 * ConstantCode class
 * 
 * @since 2019/05/23
 */
public class ConstantCode_bak {

    /* return success */
    public static final RetCode_bak SUCCESS = RetCode_bak.mark(0, "success");

    public static final RetCode_bak LOGIN_FAIL = RetCode_bak.mark(202034, "login fail");

    /* auth */
    public static final RetCode_bak USER_NOT_LOGGED_IN = RetCode_bak.mark(302000, "user not logged in");

    public static final RetCode_bak ACCESS_DENIED = RetCode_bak.mark(302001, "access denied");

    /* param exception */
    public static final RetCode_bak PARAM_EXCEPTION = RetCode_bak.mark(402000, "param exception");

}
