package com.slothdigest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SlothDigestApplication {
    public static void main(String[] args) {
        SpringApplication.run(SlothDigestApplication.class, args);
    }
}
