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
    public String getParameter(String contentParam) {
        Boolean flag = ("content".equals(contentParam) || contentParam.endsWith("WithHtml"));
        if (flag && !isIncludeRichText) {
            return super.getParameter(contentParam);
        }
        contentParam = JsoupUtil.clean(contentParam);
        String value = super.getParameter(contentParam);
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
    public String getHeader(String headParameter) {
        headParameter = JsoupUtil.clean(headParameter);
        String value = super.getHeader(headParameter);
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
