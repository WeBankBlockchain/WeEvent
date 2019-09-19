package com.webank.weevent.governance.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webank.weevent.governance.entity.BrokerEntity;
import com.webank.weevent.governance.service.BrokerService;
import com.webank.weevent.governance.service.CommonService;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ForwardWebaseFilter implements Filter {

    @Autowired
    private BrokerService brokerService;

    @Autowired
    private CommonService commonService;

    private static final String transDaily = "/group/transDaily";
    private static final String general = "/group/general";
    private static final String transList = "/transaction/transList";
    private static final String blockList = "/block/blockList";
    private static final String nodeList = "/node/nodeList";
    private static final String questionMark = "?";
    private static final String andSymbol = "&";
    private static final String layerSeparate = "/";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        String idStr = request.getParameter("brokerId");
        String originUrl = req.getRequestURI();
        // get tail of webase url
        String subStrUrl = originUrl.substring(originUrl.indexOf("/webase-node-mgr/") + "/webase-node-mgr".length());

        Integer id = Integer.parseInt(idStr);
        BrokerEntity brokerEntity = brokerService.getBroker(id);
        String webaseUrl = brokerEntity.getWebaseUrl();
        String newUrl;
        String weEventUrl = mappingWeEventUrl(subStrUrl);
        newUrl = brokerEntity.getBrokerUrl() + "/admin" + weEventUrl;
        if (webaseUrl == null || webaseUrl.length() == 0) {
            newUrl = brokerEntity.getBrokerUrl() + "/admin" + weEventUrl;
        } else {
            // get complete url of webase
            newUrl = webaseUrl + subStrUrl;
        }
        // get client according url
        CloseableHttpResponse closeResponse = commonService.getCloseResponse(req, newUrl);
        commonService.writeResponse(closeResponse, res);
    }


    private String mappingWeEventUrl(String subStrUrl) {
        String weEventUrl = "";
        if (subStrUrl.contains(this.transDaily)) {
            weEventUrl = spliceNewUrl(subStrUrl, this.transDaily);
        } else if (subStrUrl.contains(this.general)) {
            weEventUrl = spliceNewUrl(subStrUrl, this.general);
        } else if (subStrUrl.contains(this.transList)) {
            weEventUrl = spliceNewUrl(subStrUrl, this.transList);
        } else if (subStrUrl.contains(this.blockList)) {
            weEventUrl = spliceNewUrl(subStrUrl, this.blockList);
        } else if (subStrUrl.contains(this.nodeList)) {
            weEventUrl = spliceNewUrl(subStrUrl, this.nodeList);

        }
        return weEventUrl;
    }

    private String spliceNewUrl(String subStrUrl, String key) {
        if (subStrUrl.contains(this.questionMark)) {
            String midUrl = subStrUrl.substring(subStrUrl.indexOf(key) + key.length() + 1, subStrUrl.indexOf(this.questionMark));
            String afterUrl = subStrUrl.substring(subStrUrl.indexOf(this.questionMark) + 1, subStrUrl.length() - 1);
            if (key.equals(this.transDaily) || key.equals(this.general)) {
                midUrl = new StringBuffer(this.questionMark).append("groupId=").append(midUrl).append(this.andSymbol).append(afterUrl).toString();
                return key + midUrl;
            } else {
                // if key.equals(this.blockList) || key.equals(this.nodeList) || key.equals(this.transList)
                String[] split = midUrl.split(this.layerSeparate);
                midUrl = new StringBuffer(this.questionMark).append("groupId=").append(split[0]).append(this.andSymbol)
                        .append("pageNumber=").append(split[1]).append(this.andSymbol).append("pageSize=").append(split[2])
                        .append(this.andSymbol).append(afterUrl).toString();
                return key + midUrl;
            }
        } else {
            String midUrl = subStrUrl.substring(subStrUrl.indexOf(key) + key.length() + 1);
            if (key.equals(this.transDaily) || key.equals(this.general)) {
                midUrl = new StringBuffer(this.questionMark).append("groupId=").append(midUrl).toString();
                return key + midUrl;
            } else {
                // if key.equals(this.blockList) || key.equals(this.nodeList) || key.equals(this.transList)
                String[] split = midUrl.split(this.layerSeparate);
                midUrl = new StringBuffer(this.questionMark).append("groupId=").append(split[0]).append(this.andSymbol)
                        .append("pageNumber=").append(split[1]).append(this.andSymbol).append("pageSize=").append(split[2])
                        .toString();
                return key + midUrl;
            }
        }

    }

}
