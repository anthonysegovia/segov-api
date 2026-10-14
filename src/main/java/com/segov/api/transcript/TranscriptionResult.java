package com.segov.api.transcript;

import java.util.List;

public record TranscriptionResult(
		String text,
		String language,
		List<TranscriptionSegmentResult> segments
) {
}