package com.starrocks.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties
public class StarrocksAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(StarrocksAdminApplication.class, args);
    }
}
