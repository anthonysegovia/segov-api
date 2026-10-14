package com.segov.api.clip;

public record ClipCandidateGenerationResult(
		Long startMs,
		Long endMs,
		String title,
		String reason,
		Double score
) {
}