package weevent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.context.ApplicationContext;

@Slf4j
@SpringBootApplication
public class RobustApplication {

    @Autowired
    public static ApplicationContext applicationContext;

    @Autowired
    public void setContext(ApplicationContext context) {
        applicationContext = context;
    }

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(RobustApplication.class);
        app.addListeners(new ApplicationPidFileWriter());
        app.run(args);
        log.info("Start robust success");
    }

}
