package com.mygordienko.brennon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class BrennonApplication {

	public static void main(String[] args) {
		SpringApplication.run(BrennonApplication.class, args);
	}

}
