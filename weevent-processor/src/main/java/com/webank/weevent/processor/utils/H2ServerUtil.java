package com.webank.weevent.governance.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import org.h2.tools.Server;

public class H2ServerUtil {

    private static String getActiveFile() throws IOException {
        Properties properties = new Properties();
        URL url = H2ServerUtil.class.getClassLoader().getResource("application.properties");
        properties.load(new FileInputStream(url.getFile()));
        return properties.getProperty("spring.profiles.active");
    }

    public static void startH2() throws Exception {
        Properties properties = new Properties();
        String active = getActiveFile();
        URL url = H2ServerUtil.class.getClassLoader().getResource("application-" + active + ".properties");
        properties.load(new FileInputStream(url.getFile()));
        String databaseType = properties.getProperty("spring.jpa.database");
        if (("h2").equals(databaseType.toLowerCase())) {
            Server.createTcpServer(new String[]{"-tcp", "-tcpAllowOthers", "-tcpPort", "7083"}).start();

        }
    }

}
