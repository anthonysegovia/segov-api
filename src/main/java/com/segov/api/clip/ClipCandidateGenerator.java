package com.segov.api.clip;

import java.util.List;

import com.segov.api.transcript.TranscriptSegment;

public interface ClipCandidateGenerator {

	List<ClipCandidateGenerationResult> generate(List<TranscriptSegment> segments);
}