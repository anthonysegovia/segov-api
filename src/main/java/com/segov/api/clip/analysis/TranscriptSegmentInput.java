package com.segov.api.clip.analysis;

public record TranscriptSegmentInput(
		Long startMs,
		Long endMs,
		String text
) {
}