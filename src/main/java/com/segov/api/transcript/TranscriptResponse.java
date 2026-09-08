package com.segov.api.transcript;

import java.time.LocalDateTime;

public record TranscriptResponse(
		Long id,
		String text,
		String language,
		LocalDateTime createdAt
) {
}