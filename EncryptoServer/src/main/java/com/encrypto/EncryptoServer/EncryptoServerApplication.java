package com.encrypto.EncryptoServer;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static java.util.TimeZone.getTimeZone;
import static java.util.TimeZone.setDefault;

@SpringBootApplication
public class EncryptoServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EncryptoServerApplication.class, args);
        // Print listening to console
        System.out.println("Listening...");
    }

    @PostConstruct
    public void init() {
        // Set default timezone to UTC
        setDefault(getTimeZone("UTC"));
    }
}
