package com.app.trlapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
@EnableCaching
@SpringBootApplication
public class TrlappApplication {

	public static void main(String[] args) {
		SpringApplication.run(TrlappApplication.class, args);
	}

}
