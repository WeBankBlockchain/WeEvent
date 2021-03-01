package com.webank.weevent.broker.utils;

import java.io.IOException;
import java.io.InputStream;

import com.webank.weevent.client.BrokerException;
import com.webank.weevent.client.ErrorCode;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class SSL {

    public static SslContext getSSLContext(String caCertFile, String sslCertFile, String sslKeyFile, Boolean needAuthClient) throws BrokerException {
        log.info("getSSLContext form crt file:{},{},{}" , caCertFile, sslCertFile, sslKeyFile);
        InputStream caCert = null;
        InputStream sslCert = null;
        InputStream sslKey = null;

        try {
            caCert = SSL.class.getClassLoader().getResourceAsStream(caCertFile);
            sslCert = SSL.class.getClassLoader().getResourceAsStream(sslCertFile);
            sslKey = SSL.class.getClassLoader().getResourceAsStream(sslKeyFile);

            ClientAuth clientAuth = needAuthClient? ClientAuth.REQUIRE:ClientAuth.NONE;

            return SslContextBuilder.forServer(sslCert, sslKey)
                    .trustManager(caCert)
                    //.keyManager(sslCert, sslKey)
                    .clientAuth(clientAuth)
                    .build();
        } catch (Exception e) {
            log.error("init ssl context error:{}", e.toString());
            throw new BrokerException(ErrorCode.MQTT_SSL_ERROR);
        } finally {
            if (caCert != null) {
                try {
                    caCert.close();
                } catch (IOException e) {
                }
            }
            if (sslCert != null) {
                try {
                    sslCert.close();
                } catch (IOException e) {
                }
            }
            if (sslKey != null) {
                try {
                    sslKey.close();
                } catch (IOException e) {
                }
            }
        }
    }


}
