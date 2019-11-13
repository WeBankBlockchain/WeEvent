package com.webank.weevent.governance.controller;

import javax.servlet.http.HttpServletRequest;

import com.webank.weevent.governance.exception.GovernanceException;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

@Data
class SimpleException {
    /**
     * Error code.
     */
    private int code;

    /**
     * Error message.
     */
    private String message;
}

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
    @ExceptionHandler(value = GovernanceException.class)
    public Object baseErrorHandler(HttpServletRequest req, GovernanceException e) {
        log.error("detect GovernanceException", e);

        SimpleException simpleException = new SimpleException();
        simpleException.setCode(e.getCode());
        simpleException.setMessage(e.getMessage());
        log.error("rest api GovernanceException, remote: {} uri: {} {}", req.getRemoteHost(), req.getRequestURL(),
                simpleException);
        return simpleException;
    }

    @ExceptionHandler(value = Exception.class)
    public Object baseErrorHandler(HttpServletRequest req, Exception e) {
        log.error("detect Exception", e);

        log.error("rest api Exception, remote: {} uri: {} {}", req.getRemoteHost(), req.getRequestURL(),
                e.getMessage());
        return e;
    }
}
