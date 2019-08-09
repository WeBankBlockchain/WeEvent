package weevent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class RobustApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(RobustApplication.class);
        app.run(args);
    }

}
