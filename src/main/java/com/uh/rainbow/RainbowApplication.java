package com.uh.rainbow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class RainbowApplication {

    public static void main(String[] args) {
        SpringApplication.run(RainbowApplication.class, args);
    }

}
