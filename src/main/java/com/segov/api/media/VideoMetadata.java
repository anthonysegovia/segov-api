package com.segov.api.media;

public record VideoMetadata(
		Double durationSeconds,
		Integer width,
		Integer height,
		String formatName
) {
}