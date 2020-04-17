package com.webank.weevent.broker.filter;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webank.weevent.broker.config.WeEventConfig;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


/**
 * @author websterchen
 * @version 1.0
 * @since 2018/12/13
 */
@Slf4j
public class HttpInterceptorConfig implements WebMvcConfigurer {
    private WeEventConfig weEventConfig;

    static class HttpInterceptor implements HandlerInterceptor {
        private final String ipWhiteList;
        private final static String UNKNOWN = "unknown";

        HttpInterceptor(String ipWhiteList) {
            log.info("client ip white list: {}", ipWhiteList);
            this.ipWhiteList = ipWhiteList;
        }

        private String getIpAddress(HttpServletRequest request) {
            String ip = request.getHeader("X-Forwarded-For");
            log.debug("HttpServletRequest [X-Forwarded-For]: {}", ip);
            if (StringUtils.isBlank(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
                log.debug("HttpServletRequest [getRemoteAddr]: {}", ip);
                return request.getRemoteAddr();
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
        public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) {
            if (StringUtils.isBlank(this.ipWhiteList)) {
                return true;
            }

            String ip = getIpAddress(httpServletRequest);
            log.debug("ip white list:{} client ip:{}", this.ipWhiteList, ip);
            if (ip.contains("0:0:0:0") || ip.contains("127.0.0.1") || ip.contains("localhost")) {
                return true;
            }

            if (!this.ipWhiteList.contains(ip)) {
                log.error("forbid, client ip not in white list, {} -> {}", ip, this.ipWhiteList);
                httpServletResponse.setStatus(403);
                return false;
            }
            return true;
        }
    }

    public void setWeEventConfig(WeEventConfig weEventConfig) {
        this.weEventConfig = weEventConfig;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        HttpInterceptor httpInterceptor = new HttpInterceptor(this.weEventConfig.getIpWhiteList());
        registry.addInterceptor(httpInterceptor).addPathPatterns("/**");
    }
}

