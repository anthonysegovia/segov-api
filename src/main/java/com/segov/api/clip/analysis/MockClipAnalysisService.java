package com.segov.api.clip.analysis;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class MockClipAnalysisService implements ClipAnalysisService {

	@Override
	public List<AiClipSuggestion> analyze(ClipAnalysisInput input) {
		// TODO: Replace with an AI provider-backed implementation.
		return List.of();
	}
}