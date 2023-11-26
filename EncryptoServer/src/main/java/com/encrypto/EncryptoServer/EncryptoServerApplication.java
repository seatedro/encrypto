package com.encrypto.EncryptoServer;

import static java.util.TimeZone.getTimeZone;
import static java.util.TimeZone.setDefault;

import jakarta.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EncryptoServerApplication {

    private static final Logger logger = LoggerFactory.getLogger(EncryptoServerApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(EncryptoServerApplication.class, args);
        // Print listening to console
        logger.info("Listening...");
    }

    @PostConstruct
    public void init() {
        // Set default timezone to UTC
        setDefault(getTimeZone("UTC"));
    }
}
