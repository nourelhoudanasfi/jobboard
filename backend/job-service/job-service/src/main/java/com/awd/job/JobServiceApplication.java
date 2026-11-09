package com.awd.job;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Job and category management (microservice). Registers itself in Eureka (the Eureka client is enabled by the starter on the classpath). */
@SpringBootApplication
public class JobServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(JobServiceApplication.class, args);
    }
}
