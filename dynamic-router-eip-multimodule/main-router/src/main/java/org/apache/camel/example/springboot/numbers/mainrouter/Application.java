package org.apache.camel.example.springboot.numbers.mainrouter;

import org.apache.camel.example.springboot.numbers.mainrouter.config.MainRouterConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(value = {MainRouterConfig.class})
public class Application {

    /**
     * Main method to start the application.  Please make us proud.
     */
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
