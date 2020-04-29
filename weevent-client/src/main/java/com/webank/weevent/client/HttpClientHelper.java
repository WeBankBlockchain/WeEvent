package com.webank.weevent.client;

import java.io.IOException;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

@Slf4j
public class HttpClientHelper {

    private final int timeout;
    private CloseableHttpClient httpClient;

    public HttpClientHelper(int timeout) {
        this.timeout = timeout;
        this.httpClient = HttpClientBuilder.create().build();
    }

    public CloseableHttpClient buildHttpClient() {
        return this.httpClient;
    }

    public <T> BaseResponse<T> invokeCGI(HttpRequestBase request, TypeReference<BaseResponse<T>> typeReference) throws BrokerException {
        long requestStartTime = System.currentTimeMillis();
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(this.timeout)
                .setSocketTimeout(this.timeout)
                .build();
        request.setConfig(requestConfig);
        try (CloseableHttpResponse httpResponse = this.httpClient.execute(request)) {
            log.info("invokeCGI {} in {} millisecond, response:{}", request.getURI(),
                    System.currentTimeMillis() - requestStartTime, httpResponse.getStatusLine().toString());
            if (HttpStatus.SC_OK != httpResponse.getStatusLine().getStatusCode()) {
                log.error("invokeCGI failed, request url:{}, msg:{}", request.getURI(), httpResponse.getStatusLine().toString());
                throw new BrokerException(ErrorCode.HTTP_RESPONSE_FAILED);
            }
            if (null == httpResponse.getEntity()) {
                log.error("invokeCGI failed, httpResponse.getEntity is null, request url:{}", request.getURI());
                throw new BrokerException(ErrorCode.HTTP_RESPONSE_ENTITY_EMPTY);
            }

            byte[] responseResult = EntityUtils.toByteArray(httpResponse.getEntity());
            BaseResponse<T> baseResponse = JsonHelper.json2Object(responseResult, typeReference);

            if (ErrorCode.SUCCESS.getCode() != baseResponse.getCode()) {
                log.error("invokeCGI failed, request url:{}, msg:{}", request.getURI(), baseResponse.getMessage());
                throw new BrokerException(baseResponse.getCode(), baseResponse.getMessage());
            }

            return baseResponse;
        } catch (IOException e) {
            log.error("invokeCGI error, request url:{}", request.getURI(), e);
            throw new BrokerException(ErrorCode.HTTP_REQUEST_EXECUTE_ERROR);
        }
    }
}
