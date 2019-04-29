package com.webank.weevent.protocol.stomp;

import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;


@Slf4j
public class HandShakeWebSocketInterceptor implements HandshakeInterceptor {
    private String ipWhiteTable;

    HandShakeWebSocketInterceptor(String ipWhiteTable) {
        this.ipWhiteTable = ipWhiteTable;
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
        if (ip == null || ip.isEmpty() || ip.equalsIgnoreCase("unknown")) {
            log.debug("unknown ServerHttpRequest host");
            return servletRequest.getRemoteAddress().getHostString();
        }

        String[] ips = ip.split(",");
        for (String strIp : ips) {
            if (!strIp.equalsIgnoreCase("unknown")) {
                return strIp;
            }
        }
        return "";
    }


    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) {
    	String ip = getIpAddress(request);
        if (this.ipWhiteTable.equals("")) {
            return true;
        }

        if (request instanceof ServletServerHttpRequest) {
            log.debug("ip white list:{} client ip:{}", this.ipWhiteTable, ip);
            if (ip.contains("0:0:0:0") || ip.contains("127.0.0.1") || ip.contains("localhost")) {
                return true;
            }

            if (!this.ipWhiteTable.contains(ip)) {
                response.setStatusCode(HttpStatus.FORBIDDEN);
                response.close();
                log.error("forbid,client ip:{} not in white table:{}", ip, this.ipWhiteTable);
                return false;
            }
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
                               Exception exception) {
        // TODO Auto-generated method stub

    }
}
