/**
 * Copyright 2014-2019  the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webank.weevent.governance.utils;

import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.webank.weevent.governance.properties.ConstantProperties;

/**
 * cookie methods.
 */
@Component
public class CookiesTools {

    @Autowired
    private ConstantProperties constantProperties;

    /**
     * reset cookie by cookieName.
     */
    public void reSetCookie(HttpServletRequest request, HttpServletResponse response, String name) {
        String value = null;
        // 根据名字获取cookie
        Cookie cookie = getCookieByName(request, name);
        if (cookie != null) {
            // 获取cookie的值
            value = cookie.getValue();
        }
        if (StringUtils.isNotBlank(value)) {
            addCookie(request, response, name, value);
        }

    }

    /**
     * set cookie value.
     */
    public void addCookie(HttpServletRequest request, HttpServletResponse response, String name,
        String value) {
        // 默认cookie路径
        // String path = request.getContextPath();
        String path = "/";
        // 默认cookie最大生命周期
        Integer maxAge = constantProperties.getCookieMaxAge();

        addCookie(request, response, name, value, maxAge, path);
    }

    /**
     * set cookie value.
     */
    private void addCookie(HttpServletRequest request, HttpServletResponse response, String name,
        String value, int maxAge, String path) {
        String realValue = 	value.replace("\r", "").replace("\n", "");
        if (StringUtils.isNotBlank(name) && StringUtils.isNotBlank(realValue)) {
            Cookie cookie = getCookieByName(request, name);

            // 根据name获取request中的cookie
            if (cookie == null) {
                cookie = new Cookie(name, realValue);
            } else {
                cookie.setValue(realValue);
            }

            if (request.getRequestURL().toString().startsWith("https")) {
                cookie.setSecure(true);
            } else {
                cookie.setSecure(false);
            }

            if (StringUtils.isNotBlank(path)) {
                cookie.setPath(path);
            }
            if (maxAge > 0) {
                cookie.setMaxAge(maxAge);
            }

            response.addCookie(cookie);
        }
    }

    /**
     * get value by cookieName.
     */
    public Cookie getCookieByName(HttpServletRequest request, String name) {
        Map<String, Cookie> map = readCookieMap(request);
        if (map.containsKey(name)) {
            Cookie cookie = map.get(name);
            return cookie;
        }
        return null;
    }

    /**
     * get all cookie.
     */
    private Map<String, Cookie> readCookieMap(HttpServletRequest request) {
        Map<String, Cookie> map = new HashMap<String, Cookie>();
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                String name = cookie.getName();
                map.put(name, cookie);
            }
        }
        return map;
    }

    /**
     * delete by cookieName.
     */
    public void deleteCookieByName(String cookieName, HttpServletResponse response) {
        Cookie newCookie = new Cookie(cookieName, null);
        newCookie.setMaxAge(0);
        newCookie.setPath("/");
        response.addCookie(newCookie);
    }

    /**
     * get value by cookieName.
     */
    public String getCookieValueByName(HttpServletRequest request, String name) {
        Cookie cookie = getCookieByName(request, name);
        if (null == cookie) {
            return null;
        }
        return cookie.getValue();
    }

    /**
     * clear all cookie
     */
    public void clearAllCookie(HttpServletRequest request,HttpServletResponse response){
        Cookie[] cookies=request.getCookies();
        if(cookies == null || cookies.length==0){
            return;
        }
        for(Cookie cookie:cookies){
            cookie.setMaxAge(0);
            response.addCookie(cookie);

        }
    }
}
