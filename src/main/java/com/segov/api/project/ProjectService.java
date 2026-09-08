package com.segov.api.project;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class ProjectService {

	private final ProjectRepository projectRepository;

	public ProjectService(ProjectRepository projectRepository) {
		this.projectRepository = projectRepository;
	}

	public Project createProject(String name) {
		Project project = new Project();
		project.setName(name);
		return projectRepository.save(project);
	}

	public List<Project> getAllProjects() {
		return projectRepository.findAll();
	}

	public Project getProjectById(Long id) {
		return projectRepository.findById(id)
				.orElseThrow(() -> new ProjectNotFoundException(id));
	}

	public Project updateProject(Long id, String name) {
		Project project = projectRepository.findById(id)
				.orElseThrow(() -> new ProjectNotFoundException(id));
		project.setName(name);
		return projectRepository.save(project);
	}

	public void deleteProject(Long id) {
		if (!projectRepository.existsById(id)) {
			throw new ProjectNotFoundException(id);
		}

		projectRepository.deleteById(id);
	}
}