package com.webank.weevent.governance.filter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import com.webank.weevent.governance.filter.util.JsoupUtil;

import org.apache.commons.lang3.StringUtils;

/**
 * <code>{@link XssHttpServletRequestWrapper}</code>
 * 
 * @author
 */
public class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {

   private  HttpServletRequest orgRequest = null;

    private boolean isIncludeRichText = false;

    public XssHttpServletRequestWrapper(HttpServletRequest request, boolean isIncludeRichText) {
        super(request);
        orgRequest = request;
        this.isIncludeRichText = isIncludeRichText;
    }

    @Override
    public String getParameter(String parameter) {
        Boolean flag = ("content".equals(parameter) || parameter.endsWith("WithHtml"));
        if (flag && !isIncludeRichText) {
            return super.getParameter(parameter);
        }
        parameter = JsoupUtil.clean(parameter);
        String value = super.getParameter(parameter);
        if (StringUtils.isNotBlank(value)) {
            value = JsoupUtil.clean(value);
        }
        return value;
    }

    @Override
    public String[] getParameterValues(String params) {
        String[] arr = super.getParameterValues(params);
        if (arr != null) {
            for (int i = 0; i < arr.length; i++) {
                arr[i] = JsoupUtil.clean(arr[i]);
            }
        }
        return arr;
    }

    @Override
    public String getHeader(String parameter) {
        parameter = JsoupUtil.clean(parameter);
        String value = super.getHeader(parameter);
        if (StringUtils.isNotBlank(value)) {
            value = JsoupUtil.clean(value);
        }
        return value;
    }

    public HttpServletRequest getOrgRequest() {
        return orgRequest;
    }

    public static HttpServletRequest getOrgRequest(HttpServletRequest req) {
        if (req instanceof XssHttpServletRequestWrapper) {
            return ((XssHttpServletRequestWrapper) req).getOrgRequest();
        }

        return req;
    }

}
