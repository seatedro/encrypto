package com.encrypto.EncryptoServer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EncryptoServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(EncryptoServerApplication.class, args);
		// Print listening to console
		System.out.println("Listening...");
	}

}
