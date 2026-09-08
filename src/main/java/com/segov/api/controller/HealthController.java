package com.segov.api.controller;

import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HealthController {

	@GetMapping("/health")
	@Operation(summary = "Check API health")
	@ApiResponse(responseCode = "200", description = "API is available")
	public Map<String, String> health() {
		return Map.of(
				"status", "UP",
				"application", "Segov API"
		);
	}
}