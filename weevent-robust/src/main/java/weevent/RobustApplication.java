package weevent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.ApplicationPidFileWriter;

@Slf4j
@SpringBootApplication
public class RobustApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(RobustApplication.class);
        app.addListeners(new ApplicationPidFileWriter());
        app.run(args);
        log.info("Start robust success");
    }

}
