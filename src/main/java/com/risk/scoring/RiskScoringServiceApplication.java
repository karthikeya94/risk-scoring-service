package com.risk.scoring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableDiscoveryClient
@SpringBootApplication
@EnableAsync
public class RiskScoringServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RiskScoringServiceApplication.class, args);
    }

}
