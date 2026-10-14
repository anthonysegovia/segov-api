package com.segov.api.clip.analysis;

public record AiClipSuggestion(
		Long startMs,
		Long endMs,
		String title,
		String reason,
		Double score
) {
}