package com.segov.api.clip.analysis;

import java.util.List;

public interface ClipAnalysisService {

	List<AiClipSuggestion> analyze(ClipAnalysisInput input);
}