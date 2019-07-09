package com.webank.weevent.broker.fisco.util;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import com.webank.weevent.BrokerApplication;

import lombok.extern.slf4j.Slf4j;

/**
 * @author websterchen
 * @version 1.0
 * @since 2019/4/1
 */
@Slf4j
public class SystemInfoUtils {
    public static String getCurrentIp() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface ni = networkInterfaces.nextElement();
                Enumeration<InetAddress> nias = ni.getInetAddresses();
                while (nias.hasMoreElements()) {
                    InetAddress ia = nias.nextElement();
                    if (!ia.isLinkLocalAddress() && !ia.isLoopbackAddress() && ia instanceof Inet4Address) {
                        return ia.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            log.error("get local ip failed", e);
        }

        return "";
    }

    public static String getCurrentPort() {
        return BrokerApplication.environment.getProperty("server.port");
    }
}
