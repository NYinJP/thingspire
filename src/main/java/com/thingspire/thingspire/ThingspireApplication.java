package com.thingspire.thingspire;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
public class ThingspireApplication {

	public static void main(String[] args) {
		SpringApplication.run(ThingspireApplication.class, args);
	}

}
