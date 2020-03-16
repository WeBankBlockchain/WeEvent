package com.webank.weevent.governance.common;

import lombok.Getter;
import lombok.Setter;

/**
 * class about exception code and message.
 * 
 * @since 2019/05/23
 */
@Getter
@Setter
public class RetCode {

    private Integer code;

    private String msg;

    public RetCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public static RetCode mark(int code, String msg) {
        return new RetCode(code, msg);
    }

    public static RetCode mark(Integer code) {
        return new RetCode(code, null);
    }
}
