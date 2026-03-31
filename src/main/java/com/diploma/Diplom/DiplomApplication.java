package com.diploma.Diplom;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
@ConfigurationPropertiesScan
@SpringBootApplication
public class DiplomApplication {

	public static void main(String[] args) {
		SpringApplication.run(DiplomApplication.class, args);
	}

}
