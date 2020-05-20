package com.webank.weevent.client.stomp;


import java.net.URI;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;

import com.webank.weevent.client.BrokerException;
import com.webank.weevent.client.ErrorCode;
import com.webank.weevent.client.WeEventClient;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * WebSocketTransport factory.
 *
 * @author matthewliu
 * @since 2019/04/02
 */
@Slf4j
@Getter
@Setter
public class WebSocketTransportFactory {
    private static int heartbeat = 30;

    public static WebSocketTransport create(URI uri, int timeout) throws BrokerException {
        try {
            WebSocketTransport client = new WebSocketTransport(uri);
            if (uri.toString().startsWith("wss")) {
                log.info("tls transport");

                SSLContext sslContext = WeEventClient.getSSLContext();
                client.setSocketFactory(sslContext.getSocketFactory());
            }

            client.setTcpNoDelay(true);
            client.setConnectionLostTimeout(heartbeat);
            client.setTimeout(timeout);

            // block connect
            boolean result = client.connectBlocking(timeout, TimeUnit.SECONDS);
            if (!result) {
                log.error("connect to remote failed, {}", uri.toString());
                throw new BrokerException(ErrorCode.URL_CONNECT_FAILED);
            }

            log.info("connect to remote success, {}", uri.toString());
            return client;
        } catch (InterruptedException e) {
            log.error("interrupted while connecting, {}", uri.toString());
            Thread.currentThread().interrupt();
            throw new BrokerException(ErrorCode.URL_CONNECT_FAILED);
        }
    }
}
