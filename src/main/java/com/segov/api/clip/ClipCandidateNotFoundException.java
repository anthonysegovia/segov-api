package com.segov.api.clip;

public class ClipCandidateNotFoundException extends RuntimeException {

	public ClipCandidateNotFoundException(Long candidateId) {
		super("Clip candidate not found: " + candidateId);
	}
}