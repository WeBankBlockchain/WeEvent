package com.webank.weevent.governance;

import lombok.extern.slf4j.Slf4j;
import org.h2.tools.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * spring boot start test
 *
 * @since 2018/12/18
 */
@Slf4j
@SpringBootApplication
@EnableTransactionManagement
public class GovernanceApplication {
    public static void main(String[] args) throws Exception {
        startH2();
        SpringApplication app = new SpringApplication(GovernanceApplication.class);
        app.addListeners(new ApplicationPidFileWriter());
        app.run(args);
        log.info("Start Governance success");
    }

    private static void startH2() throws Exception {
        Server server = Server.createTcpServer(new String[]{"-tcp", "-tcpAllowOthers", "-tcpPort", "7082"}).start();
        String status = server.getStatus();
        log.info("h2 status:{}", status);
    }

}
