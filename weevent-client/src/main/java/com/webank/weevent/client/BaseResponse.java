package com.webank.weevent.client;

import lombok.Getter;
import lombok.Setter;

/**
 * Base wrapper fields in rest response. like:
 * {
 * "code": 0,
 * "message": "success",
 * "data": {}
 * }
 *
 * @author matthewliu
 * @since 2019/12/26
 */
@Getter
@Setter
public class BaseResponse<T> {
    private int code;
    private String message;
    private T data;

    private BaseResponse() {
    }

    public static BaseResponse<?> buildSuccess() {
        BaseResponse<?> baseResponse = new BaseResponse<>();
        baseResponse.code = ErrorCode.SUCCESS.getCode();
        baseResponse.message = ErrorCode.SUCCESS.getCodeDesc();
        return baseResponse;
    }

    public static <T> BaseResponse<T> buildSuccess(T data) {
        BaseResponse<T> baseResponse = new BaseResponse<>();
        baseResponse.code = ErrorCode.SUCCESS.getCode();
        baseResponse.message = ErrorCode.SUCCESS.getCodeDesc();
        baseResponse.data = data;
        return baseResponse;
    }

    public static <T> BaseResponse<T> buildFail(ErrorCode errorCode) {
        BaseResponse<T> baseResponse = new BaseResponse<>();
        baseResponse.code = errorCode.getCode();
        baseResponse.message = errorCode.getCodeDesc();
        return baseResponse;
    }

    public static <T> BaseResponse<T> buildException(BrokerException exp) {
        BaseResponse<T> baseResponse = new BaseResponse<>();
        baseResponse.code = exp.getCode();
        baseResponse.message = exp.getMessage();
        return baseResponse;
    }

    public static <T> BaseResponse<T> buildException(Exception exp) {
        BaseResponse<T> baseResponse = new BaseResponse<>();
        baseResponse.code = ErrorCode.UNKNOWN_ERROR.getCode();
        baseResponse.message = exp.getMessage();
        return baseResponse;
    }
}