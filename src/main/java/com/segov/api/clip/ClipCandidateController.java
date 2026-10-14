package com.segov.api.clip;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ClipCandidateController {

	private final ClipCandidateService clipCandidateService;

	public ClipCandidateController(ClipCandidateService clipCandidateService) {
		this.clipCandidateService = clipCandidateService;
	}

	@PostMapping("/api/videos/{videoId}/clip-candidates/generate")
	public ResponseEntity<List<ClipCandidateResponse>> generateClipCandidates(@PathVariable Long videoId) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(toResponses(clipCandidateService.generateForVideo(videoId)));
	}

	@GetMapping("/api/videos/{videoId}/clip-candidates")
	public ResponseEntity<List<ClipCandidateResponse>> getClipCandidates(@PathVariable Long videoId) {
		return ResponseEntity.ok(toResponses(clipCandidateService.getByVideoId(videoId)));
	}

	private List<ClipCandidateResponse> toResponses(List<ClipCandidate> candidates) {
		return candidates.stream()
				.map(candidate -> new ClipCandidateResponse(
						candidate.getId(),
						candidate.getStartMs(),
						candidate.getEndMs(),
						candidate.getTitle(),
						candidate.getReason(),
						candidate.getScore(),
						candidate.getGenerationSource(),
						candidate.getStatus()
				))
				.toList();
	}
}