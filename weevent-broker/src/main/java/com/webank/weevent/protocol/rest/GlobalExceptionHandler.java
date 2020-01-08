package com.webank.weevent.protocol.rest;


import javax.servlet.http.HttpServletRequest;

import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.ErrorCode;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

/**
 * Global exception handler for restful.
 *
 * @author matthewliu
 * @since 2019/02/01
 */
@Slf4j
@ControllerAdvice
@RestController
public class GlobalExceptionHandler {
    @ExceptionHandler(value = BrokerException.class)
    public Object baseErrorHandler(HttpServletRequest req, BrokerException e) {
        log.error("rest api BrokerException, remote: {} uri: {}", req.getRemoteHost(), req.getRequestURL());
        log.error("detect BrokerException", e);
        return e;
    }

    @ExceptionHandler(value = Exception.class)
    public Object baseErrorHandler(HttpServletRequest req, Exception e) {
        log.error("rest api Exception, remote: {} uri: {}", req.getRemoteHost(), req.getRequestURL());
        log.error("detect Exception", e);
        return new BrokerException(ErrorCode.UNKNOWN_ERROR.getCode(), e.getMessage());
    }
}
