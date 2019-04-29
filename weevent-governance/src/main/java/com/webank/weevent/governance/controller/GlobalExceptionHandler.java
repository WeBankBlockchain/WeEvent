package com.webank.weevent.governance.controller;


import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import lombok.Data;

/**
 * Global exception handler for restful.
 *
 * @author v_wbjnzhang
 * @since 2019/03/14
 */

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

@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public SimpleException jsonErrorHandler(HttpServletRequest req, Exception e) throws Exception {
        SimpleException r = new SimpleException();
        String msg = e.getMessage();
        //remove input string return
        msg = msg.substring(0, msg.indexOf("nested exception"));
        r.setMessage(msg);
        r.setCode(100);
        return r;
    }



}
