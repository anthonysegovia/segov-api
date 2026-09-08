package com.segov.api;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
@OpenAPIDefinition(info = @Info(
		title = "Segov API",
		description = "Backend API for Segov, an AI-powered platform for creating and publishing short-form content.",
		version = "v1"
))
public class SegovApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(SegovApiApplication.class, args);
	}

}
