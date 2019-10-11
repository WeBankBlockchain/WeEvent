package com.webank.weevent.governance.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webank.weevent.governance.code.ErrorCode;
import com.webank.weevent.governance.exception.GovernanceException;
import com.webank.weevent.governance.utils.SpringContextUtil;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.helper.StringUtil;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CommonService implements AutoCloseable {

    public static final String HTTPS = "https";
    public static final String HTTPS_CLIENT = "httpsClient";
    public static final String HTTP_CLIENT = "httpClient";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String METHOD_TYPE = "GET";
    public static final String FORMAT_TYPE = "json";

    public static final String QUESTION_MARK = "?";
    public static final String AND_SYMBOL = "&";
    public static final String LAYER_SEPARATE = "/";
    public static final String EQUAL_SIGN = "=";


    public CloseableHttpResponse getCloseResponse(HttpServletRequest req, String newUrl) throws ServletException {
        CloseableHttpResponse closeResponse;
        try {
            CloseableHttpClient client = this.generateHttpClient(newUrl);
            if (req.getMethod().equals(METHOD_TYPE)) {
                HttpGet get = this.getMethod(newUrl, req);
                closeResponse = client.execute(get);
            } else {
                HttpPost postMethod = this.postMethod(newUrl, req);
                closeResponse = client.execute(postMethod);
            }
        } catch (Exception e) {
            log.error("getCloseResponse fail,error:{}", e.getMessage());
            throw new ServletException(e.getMessage());
        }
        return closeResponse;
    }

    public HttpGet getMethod(String uri, HttpServletRequest request) throws GovernanceException {
        try {
            URIBuilder builder = new URIBuilder(uri);
            Enumeration<String> enumeration = request.getParameterNames();
            while (enumeration.hasMoreElements()) {
                String nex = enumeration.nextElement();
                builder.setParameter(nex, request.getParameter(nex));
            }
            return new HttpGet(builder.build());
        } catch (URISyntaxException e) {
            log.error("build url method fail,error:{}", e.getMessage());
            throw new GovernanceException(ErrorCode.BUILD_URL_METHOD);
        }
    }

    private HttpPost postMethod(String uri, HttpServletRequest request) {
        StringEntity entity;
        if (request.getContentType().contains(FORMAT_TYPE)) {
            entity = this.jsonData(request);
        } else {
            entity = this.formData(request);
        }
        HttpPost httpPost = new HttpPost(uri);
        httpPost.setHeader(CONTENT_TYPE, request.getHeader(CONTENT_TYPE));
        httpPost.setEntity(entity);
        return httpPost;
    }

    private StringEntity jsonData(HttpServletRequest request) {
        try (InputStreamReader is = new InputStreamReader(request.getInputStream(), request.getCharacterEncoding());
             BufferedReader reader = new BufferedReader(is)) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return new StringEntity(sb.toString(), request.getCharacterEncoding());
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    private UrlEncodedFormEntity formData(HttpServletRequest request) {
        UrlEncodedFormEntity urlEncodedFormEntity = null;
        try {
            List<NameValuePair> pairs = new ArrayList<>();
            Enumeration<String> params = request.getParameterNames();
            while (params.hasMoreElements()) {
                String name = params.nextElement();
                pairs.add(new BasicNameValuePair(name, request.getParameter(name)));
            }

            urlEncodedFormEntity = new UrlEncodedFormEntity(pairs, request.getCharacterEncoding());
        } catch (UnsupportedEncodingException e) {
            log.error(e.getMessage());
        }
        return urlEncodedFormEntity;
    }

    /**
     * return HttpGet
     */

    // generate CloseableHttpClient from url
    public CloseableHttpClient generateHttpClient(String url) {
        CloseableHttpClient bean;
        if (url.startsWith(HTTPS)) {
            bean = (CloseableHttpClient) SpringContextUtil.getBean(HTTPS_CLIENT);
        } else {
            bean = (CloseableHttpClient) SpringContextUtil.getBean(HTTP_CLIENT);
        }
        return bean;
    }

    public void writeResponse(CloseableHttpResponse closeResponse, HttpServletResponse res) throws IOException {
        String mes = EntityUtils.toString(closeResponse.getEntity());
        log.info("response: " + mes);
        Header encode = closeResponse.getFirstHeader(CONTENT_TYPE);
        res.setHeader(encode.getName(), encode.getValue());
        ServletOutputStream out = res.getOutputStream();
        out.write(mes.getBytes());
    }


    public void checkDataBaseUrl(String dataBaseUrl) throws GovernanceException {
        if (StringUtil.isBlank(dataBaseUrl)) {
            return;
        }
        String defaultUrl = dataBaseUrl.substring(0, dataBaseUrl.indexOf(this.QUESTION_MARK));
        String user = dataBaseUrl.substring(dataBaseUrl.indexOf(this.QUESTION_MARK) + 1, dataBaseUrl.indexOf(this.AND_SYMBOL));
        user = user.split(this.EQUAL_SIGN)[1].replaceAll("\"", "");
        int first = dataBaseUrl.indexOf(this.AND_SYMBOL);
        int second = dataBaseUrl.indexOf(this.AND_SYMBOL, first + 1);
        int last = dataBaseUrl.lastIndexOf(this.AND_SYMBOL);
        String password = dataBaseUrl.substring(first, second);
        password = password.split(this.EQUAL_SIGN)[1].replaceAll("\"", "");
        try (Connection conn = DriverManager.getConnection(defaultUrl, user, password)) {
            if (conn != null) {
                log.info("database connect success,dataBaseUrl:{}", dataBaseUrl);
            }
        } catch (Exception e) {
            log.error("database url is error", e);
            throw new GovernanceException("database url is error", e);
        }
    }





    @Override
    public void close() throws Exception {
        log.info("resource is close");
    }
}
