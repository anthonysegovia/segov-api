package com.segov.api.transcript;

public record TranscriptSegmentResponse(
		Long id,
		Long startMs,
		Long endMs,
		String text
) {
}