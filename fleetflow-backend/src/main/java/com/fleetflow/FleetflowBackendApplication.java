
package com.fleetflow;

import com.fleetflow.messaging.config.MessagingProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(MessagingProperties.class)
public class FleetflowBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(FleetflowBackendApplication.class, args);
    }
}
