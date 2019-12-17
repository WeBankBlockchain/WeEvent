package com.webank.weevent.governance.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.apache.http.client.config.RequestConfig;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CommonService implements AutoCloseable {

    public static final String HTTPS = "https";
    public static final String HTTP = "http";
    public static final String HTTPS_CLIENT = "httpsClient";
    public static final String HTTP_CLIENT = "httpClient";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String METHOD_TYPE = "GET";
    public static final String FORMAT_TYPE = "json";

    @Value("${http.client.connection-request-timeout:3000}")
    private int connectionRequestTimeout;

    @Value("${http.client.connection-request-timeout:3000}")
    private int connectionTimeout;

    @Value("${http.client.socket-timeout:3000}")
    private int socketTimeout;

    @Value("${spring.datasource.url}")
    private String dataBaseUrl;

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

    public CloseableHttpResponse getCloseResponse(HttpServletRequest req, String newUrl, String jsonString) throws ServletException {
        CloseableHttpResponse closeResponse;
        try {
            log.info("url {}", newUrl);
            CloseableHttpClient client = this.generateHttpClient(newUrl);
            if (req.getMethod().equals(METHOD_TYPE)) {
                HttpGet get = this.getMethod(newUrl, req);
                closeResponse = client.execute(get);
            } else {
                HttpPost postMethod = this.postMethod(newUrl, req, jsonString);
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
            HttpGet httpGet = new HttpGet(builder.build());
            httpGet.setConfig(getRequestConfig());
            return httpGet;
        } catch (URISyntaxException e) {
            log.error("build url method fail,error:{}", e.getMessage());
            throw new GovernanceException(ErrorCode.BUILD_URL_METHOD);
        }
    }

    private HttpPost postMethod(String uri, HttpServletRequest request, String jsonString) {
        StringEntity entity = new StringEntity(jsonString, "UTF-8");
        HttpPost httpPost = new HttpPost(uri);
        httpPost.setHeader(CONTENT_TYPE, request.getHeader(CONTENT_TYPE));
        httpPost.setEntity(entity);
        return httpPost;
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
        httpPost.setConfig(getRequestConfig());
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

    public void checkDataBaseUrl(String dataBaseUrl, String tableName, String user, String password) throws GovernanceException {
        try (Connection conn = DriverManager.getConnection(dataBaseUrl, user, password);
             Statement stat = conn.createStatement()) {
            if (stat == null) {
                log.info("database connect fail,dataBaseUrl:{}", dataBaseUrl);
                throw new GovernanceException("database connect success,dataBaseUrl:" + dataBaseUrl);
            }
            log.info("database connect success,dataBaseUrl:{}", dataBaseUrl);
            String querySql = "SELECT 1 FROM " + tableName + " LIMIT 1";
            stat.executeQuery(querySql);
        } catch (Exception e) {
            log.error("database url is error", e);
            throw new GovernanceException("database url is error", e);
        }
    }

    private RequestConfig getRequestConfig() {
        return RequestConfig.custom()
                .setConnectTimeout(connectionTimeout)
                .setSocketTimeout(socketTimeout)
                .setConnectionRequestTimeout(connectionRequestTimeout)
                .build();
    }


    public static Map<String, String> uRLRequest(String URL) {
        Map<String, String> mapRequest = new HashMap<>();

        if (StringUtil.isBlank(URL)) {
            return mapRequest;
        }
        String[] arrSplit = URL.split("[?]");
        mapRequest.put("dataBaseUrl", arrSplit[0]);
        if(arrSplit.length>1){
            mapRequest.put("optionalParameter", arrSplit[1]);
        }
        return mapRequest;
    }


    @Override
    public void close() throws Exception {
        log.info("resource is close");
    }
}
