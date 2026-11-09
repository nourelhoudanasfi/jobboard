package com.awd.candidat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Candidate and address management (microservice). Registers itself in Eureka (the Eureka client is enabled by the starter on the classpath). */
@SpringBootApplication
public class CandidatServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CandidatServiceApplication.class, args);
    }
}
