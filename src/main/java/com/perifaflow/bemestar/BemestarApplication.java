package com.perifaflow.bemestar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class BemestarApplication {

	public static void main(String[] args) {
		SpringApplication.run(BemestarApplication.class, args);
	}

}
