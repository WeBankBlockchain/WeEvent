package com.webank.weevent.client;

import java.io.IOException;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

/**
 * HttpClient utils
 */

@Slf4j
public class HttpClientUtils {
    private static final int HTTP_RESPONSE_STATUS_SUCCESS = 200;

    public static CloseableHttpClient buildHttpClient() {
        return HttpClientBuilder.create().build();
    }

    public static <T> BaseResponse<T> invokeCGI(CloseableHttpClient httpClient, HttpUriRequest request, TypeReference<BaseResponse<T>> typeReference) throws BrokerException {
        long requestStartTime = System.currentTimeMillis();
        try (CloseableHttpResponse httpResponse = httpClient.execute(request)) {
            log.info("invokeCGI {} in {} millisecond, response:{}", request.getURI(),
                    System.currentTimeMillis() - requestStartTime, httpResponse.getStatusLine().toString());
            if (HTTP_RESPONSE_STATUS_SUCCESS != httpResponse.getStatusLine().getStatusCode()) {
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
