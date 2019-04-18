package com.webank.weevent.protocol.rest;


import javax.servlet.http.HttpServletRequest;

import com.webank.weevent.BrokerApplication;
import com.webank.weevent.broker.ha.MasterJob;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.ErrorCode;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Base class for restful ha.
 *
 * @author matthewliu
 * @since 2019/03/14
 */
@Slf4j
public class RestHA {
    protected MasterJob masterJob;
    protected HttpServletRequest request;

    @Autowired(required = false)
    public void setMasterJob(MasterJob masterJob) {
        this.masterJob = masterJob;
    }

    @Autowired
    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }

    public static String getUrlFormat(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append(request.getScheme());
        sb.append("://%s");
        sb.append(BrokerApplication.environment.getProperty("server.servlet.context-path"));
        sb.append(request.getServletPath());
        if (request.getQueryString() != null) {
            sb.append("?");
            sb.append(request.getQueryString());
        }

        return sb.toString();
    }

    public void checkSupport() throws BrokerException {
        if (this.masterJob == null) {
            log.error("no broker.zookeeper.ip configuration, skip it");
            throw new BrokerException(ErrorCode.CGI_SUBSCRIPTION_NO_ZOOKEEPER);
        }
    }
}
