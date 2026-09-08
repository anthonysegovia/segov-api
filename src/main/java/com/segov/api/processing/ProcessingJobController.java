package com.segov.api.processing;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProcessingJobController {

	private final ProcessingJobService processingJobService;

	public ProcessingJobController(ProcessingJobService processingJobService) {
		this.processingJobService = processingJobService;
	}

	@PostMapping("/api/videos/{videoId}/processing-jobs")
	public ResponseEntity<ProcessingJob> createProcessingJob(@PathVariable Long videoId) {
		ProcessingJob processingJob = processingJobService.createProcessingJob(videoId);
		return ResponseEntity.status(HttpStatus.CREATED).body(processingJob);
	}

	@PostMapping("/api/processing-jobs/{jobId}/process")
	public ResponseEntity<ProcessingJob> processJob(@PathVariable Long jobId) {
		return ResponseEntity.ok(processingJobService.processJob(jobId));
	}

	@GetMapping("/api/processing-jobs/{jobId}")
	public ResponseEntity<ProcessingJob> getProcessingJobById(@PathVariable Long jobId) {
		return ResponseEntity.ok(processingJobService.getProcessingJobById(jobId));
	}
}