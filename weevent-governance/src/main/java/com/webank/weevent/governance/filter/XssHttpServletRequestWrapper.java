package com.webank.weevent.governance.filter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import com.webank.weevent.governance.utils.JsoupUtil;

import org.apache.commons.lang3.StringUtils;

/**
 * <code>{@link XssHttpServletRequestWrapper}</code>
 *
 * @author
 */
public class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {

    private HttpServletRequest orgRequest = null;

    private boolean isIncludeRichText = false;

    public XssHttpServletRequestWrapper(HttpServletRequest request, boolean isIncludeRichText) {
        super(request);
        orgRequest = request;
        this.isIncludeRichText = isIncludeRichText;
    }

    @Override
    public String getParameter(String contentParam) {
        String content = contentParam;
        Boolean flag = ("content".equals(content) || content.endsWith("WithHtml"));
        if (flag && !isIncludeRichText) {
            return super.getParameter(content);
        }
        content = JsoupUtil.clean(content);
        String value = super.getParameter(content);
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
        String params = JsoupUtil.clean(headParameter);
        String value = super.getHeader(params);
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
