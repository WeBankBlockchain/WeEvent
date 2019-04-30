package com.webank.weevent.sdk.jms;


import java.net.URI;
import java.util.concurrent.TimeUnit;

import javax.jms.JMSException;
import javax.net.ssl.SSLContext;

import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.ErrorCode;
import com.webank.weevent.sdk.WeEventClient;

import lombok.extern.slf4j.Slf4j;

/**
 * WebSocketTransport factory.
 *
 * @author matthewliu
 * @since 2019/04/02
 */
@Slf4j
public class WebSocketTransportFactory {
    private static int heartbeat = 30;

    public static void setHeartbeat(int hb) {
        heartbeat = hb;
    }

    public static WebSocketTransport create(URI uri, int timeout) throws JMSException {
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
            boolean result = client.connectBlocking(timeout, TimeUnit.SECONDS);
            if (!result) {
                log.error("connect to remote failed, {}", uri.toString());
                throw WeEventConnectionFactory.error2JMSException(ErrorCode.URL_CONNECT_FAILED);
            }

            return client;
        } catch (BrokerException e) {
            log.error("connect to remote failed, {}", uri.toString());
            throw WeEventConnectionFactory.exp2JMSException(e);
        } catch (InterruptedException e) {
            log.error("interrupted while connecting");
            throw WeEventConnectionFactory.error2JMSException(ErrorCode.URL_CONNECT_FAILED);
        }
    }
}
