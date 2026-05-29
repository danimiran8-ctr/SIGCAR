package com.rentcar.sigcar.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SigcarApplication {

    public static void main(String[] args) {
        SpringApplication.run(SigcarApplication.class, args);
    }
}