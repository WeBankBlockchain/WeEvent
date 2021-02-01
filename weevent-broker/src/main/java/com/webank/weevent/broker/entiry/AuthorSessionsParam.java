package com.webank.weevent.broker.entiry;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthorSessionsParam {

    private String clientId;
    private String userName;

}
