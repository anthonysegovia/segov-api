package com.segov.api.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProjectRequest(
		@NotBlank(message = "Project name is required")
		@Size(max = 100, message = "Project name must not exceed 100 characters")
		String name
) {
}