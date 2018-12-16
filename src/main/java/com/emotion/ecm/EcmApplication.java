package com.emotion.ecm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EcmApplication {

	public static void main(String[] args) {
		SpringApplication.run(EcmApplication.class, args);
	}
}
