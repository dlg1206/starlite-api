package com.uh.starlite;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class StarliteApplication {

    static void main(String[] args) {
        SpringApplication.run(StarliteApplication.class, args);
    }

}
