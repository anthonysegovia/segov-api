package com.segov.api.processing;

public class InvalidProcessingJobStateException extends RuntimeException {

	public InvalidProcessingJobStateException(Long jobId, ProcessingJobStatus currentStatus) {
		super("Processing job " + jobId + " cannot be processed from status " + currentStatus);
	}
}