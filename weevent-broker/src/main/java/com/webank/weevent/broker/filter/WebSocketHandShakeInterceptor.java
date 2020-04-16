package com.webank.weevent.broker.filter;

import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;


@Slf4j
public class WebSocketHandShakeInterceptor implements HandshakeInterceptor {
    private final String ipWhiteList;
    private final static String UNKNOWN = "unknown";

    WebSocketHandShakeInterceptor(String ipWhiteList) {
        log.info("client ip white list: {}", ipWhiteList);
        this.ipWhiteList = ipWhiteList;
    }

    /**
     * get request ip,and check it
     *
     * @param request request
     * @return ip address
     */
    private String getIpAddress(ServerHttpRequest request) {
        ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
        String ip = servletRequest.getServletRequest().getHeader("X-Forwarded-For");
        log.debug("ServerHttpRequest host: {}", ip);
        if (StringUtils.isBlank(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
            log.debug("unknown ServerHttpRequest host");
            return servletRequest.getRemoteAddress().getHostString();
        }

        String[] ips = ip.split(",");
        for (String strIp : ips) {
            if (!UNKNOWN.equalsIgnoreCase(strIp)) {
                return strIp;
            }
        }
        return "";
    }


    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) {
        /*
        if ("mqtt".equalsIgnoreCase(request.getHeaders().getFirst(WebSocketHttpHeaders.SEC_WEBSOCKET_PROTOCOL))) {
            log.info("MQTT over websocket DO NOT support {}, close it", WebSocketHttpHeaders.SEC_WEBSOCKET_EXTENSIONS);

            request.getHeaders().remove(WebSocketHttpHeaders.SEC_WEBSOCKET_EXTENSIONS);
        }
        */
        
        if (StringUtils.isBlank(this.ipWhiteList)) {
            return true;
        }

        String ip = this.getIpAddress(request);
        log.debug("ip white list: {} client ip: {}", this.ipWhiteList, ip);
        if (ip.contains("0:0:0:0") || ip.contains("127.0.0.1") || ip.contains("localhost")) {
            return true;
        }

        if (!this.ipWhiteList.contains(ip)) {
            log.error("forbid, client ip is not in white list, {} -> {}", ip, this.ipWhiteList);

            response.setStatusCode(HttpStatus.FORBIDDEN);
            response.close();
            return false;
        }

        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
                               Exception exception) {
        // useless
    }
}
