package com.segov.api.transcript;

public record TranscriptionSegmentResult(
		Long startMs,
		Long endMs,
		String text
) {
}