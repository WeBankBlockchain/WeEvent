package com.webank.weevent.governance.common;

import lombok.Data;

/**
 * class about exception code and message.
 * 
 * @since 2019/05/23
 */
@Data
public class RetCode_bak {

    private Integer code;

    private String msg;

    public RetCode_bak(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public static RetCode_bak mark(int code, String msg) {
        return new RetCode_bak(code, msg);
    }

    public static RetCode_bak mark(Integer code) {
        return new RetCode_bak(code, null);
    }
}
