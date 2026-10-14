package com.segov.api.clip;

public record ClipCandidateResponse(
		Long id,
		Long startMs,
		Long endMs,
		String title,
		String reason,
		Double score,
		ClipCandidateGenerationSource generationSource,
		ClipCandidateStatus status
) {
}