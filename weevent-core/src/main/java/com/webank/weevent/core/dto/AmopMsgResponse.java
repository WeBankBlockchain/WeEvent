package com.webank.weevent.core.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * AMOP channel message response.
 *
 * @author v_wbhwliu
 * @version 1.5
 * @since 2020/9/26
 */

@Getter
@Setter
public class AmopMsgResponse {

    private Integer errorCode;
    private String errorMessage;
    // message body
    private Object content;
}
