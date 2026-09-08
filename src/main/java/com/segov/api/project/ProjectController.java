package com.segov.api.project;

import java.util.List;

import com.segov.api.project.dto.CreateProjectRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

	private final ProjectService projectService;

	public ProjectController(ProjectService projectService) {
		this.projectService = projectService;
	}

	@PostMapping
	public ResponseEntity<Project> createProject(@Valid @RequestBody CreateProjectRequest request) {
		Project project = projectService.createProject(request.name());
		return ResponseEntity.status(HttpStatus.CREATED).body(project);
	}

	@GetMapping
	public ResponseEntity<List<Project>> getAllProjects() {
		return ResponseEntity.ok(projectService.getAllProjects());
	}
}