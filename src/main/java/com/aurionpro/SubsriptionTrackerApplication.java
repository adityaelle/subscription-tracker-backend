package com.aurionpro;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = {"com.aurionpro"})
public class SubsriptionTrackerApplication {
    public static void main(String[] args) {
        SpringApplication.run(SubsriptionTrackerApplication.class, args);
    }
}