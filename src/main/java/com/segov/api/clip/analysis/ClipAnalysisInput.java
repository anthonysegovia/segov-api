package com.segov.api.clip.analysis;

import java.util.List;

public record ClipAnalysisInput(
		Long sourceVideoId,
		String transcriptText,
		List<TranscriptSegmentInput> segments
) {
}