package com.segov.api.processing;

public class ProcessingJobNotFoundException extends RuntimeException {

	public ProcessingJobNotFoundException(Long id) {
		super("Processing job not found with id: " + id);
	}
}