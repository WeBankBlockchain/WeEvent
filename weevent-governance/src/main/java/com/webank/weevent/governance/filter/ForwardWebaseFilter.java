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
import com.webank.weevent.governance.properties.ConstantProperties;
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
        if (subStrUrl.contains(ConstantProperties.BROKER_TRANS_DAILY)) {
            weEventUrl = spliceNewUrl(subStrUrl, ConstantProperties.BROKER_TRANS_DAILY);
        } else if (subStrUrl.contains(ConstantProperties.BROKER_GROUP_GENERAL)) {
            weEventUrl = spliceNewUrl(subStrUrl, ConstantProperties.BROKER_GROUP_GENERAL);
        } else if (subStrUrl.contains(ConstantProperties.BROKER_TRANS_LIST)) {
            weEventUrl = spliceNewUrl(subStrUrl, ConstantProperties.BROKER_TRANS_LIST);
        } else if (subStrUrl.contains(ConstantProperties.BROKER_BLOCK_LIST)) {
            weEventUrl = spliceNewUrl(subStrUrl, ConstantProperties.BROKER_BLOCK_LIST);
        } else if (subStrUrl.contains(ConstantProperties.BROKER_NODE_LIST)) {
            weEventUrl = spliceNewUrl(subStrUrl, ConstantProperties.BROKER_NODE_LIST);

        }
        return weEventUrl;
    }

    private String spliceNewUrl(String subStrUrl, String key) {
        if (subStrUrl.contains(ConstantProperties.QUESTION_MARK)) {
            String midUrl = subStrUrl.substring(subStrUrl.indexOf(key) + key.length() + 1, subStrUrl.indexOf(ConstantProperties.QUESTION_MARK));
            String afterUrl = subStrUrl.substring(subStrUrl.indexOf(ConstantProperties.QUESTION_MARK) + 1, subStrUrl.length() - 1);
            if (key.equals(ConstantProperties.BROKER_TRANS_DAILY) || key.equals(ConstantProperties.BROKER_GROUP_GENERAL)) {
                midUrl = new StringBuffer(ConstantProperties.QUESTION_MARK).append("groupId=").append(midUrl).append(ConstantProperties.AND_SYMBOL).append(afterUrl).toString();
                return key + midUrl;
            } else {
                // if key.equals(this.blockList) || key.equals(this.nodeList) || key.equals(this.transList)
                String[] split = midUrl.split(ConstantProperties.LAYER_SEPARATE);
                midUrl = new StringBuffer(ConstantProperties.QUESTION_MARK).append("groupId=").append(split[0]).append(ConstantProperties.AND_SYMBOL)
                        .append("pageNumber=").append(split[1]).append(ConstantProperties.AND_SYMBOL).append("pageSize=").append(split[2])
                        .append(ConstantProperties.AND_SYMBOL).append(afterUrl).toString();
                return key + midUrl;
            }
        } else {
            String midUrl = subStrUrl.substring(subStrUrl.indexOf(key) + key.length() + 1);
            if (key.equals(ConstantProperties.BROKER_GROUP_GENERAL) || key.equals(ConstantProperties.BROKER_GROUP_GENERAL)) {
                midUrl = new StringBuffer(ConstantProperties.QUESTION_MARK).append("groupId=").append(midUrl).toString();
                return key + midUrl;
            } else {
                // if key.equals(this.blockList) || key.equals(this.nodeList) || key.equals(this.transList)
                String[] split = midUrl.split(ConstantProperties.LAYER_SEPARATE);
                midUrl = new StringBuffer(ConstantProperties.QUESTION_MARK).append("groupId=").append(split[0]).append(ConstantProperties.AND_SYMBOL)
                        .append("pageNumber=").append(split[1]).append(ConstantProperties.AND_SYMBOL).append("pageSize=").append(split[2])
                        .toString();
                return key + midUrl;
            }
        }

    }

}
