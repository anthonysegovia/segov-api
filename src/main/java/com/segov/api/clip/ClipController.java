package com.segov.api.clip;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ClipController {

	private final ClipService clipService;

	public ClipController(ClipService clipService) {
		this.clipService = clipService;
	}

	@PostMapping("/api/clip-candidates/{candidateId}/generate")
	public ResponseEntity<ClipResponse> generateClip(@PathVariable Long candidateId) {
		return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(clipService.generateFromCandidate(candidateId)));
	}

	@GetMapping("/api/videos/{videoId}/clips")
	public ResponseEntity<List<ClipResponse>> getClipsByVideoId(@PathVariable Long videoId) {
		return ResponseEntity.ok(clipService.getByVideoId(videoId).stream().map(this::toResponse).toList());
	}

	private ClipResponse toResponse(Clip clip) {
		return new ClipResponse(
				clip.getId(),
				clip.getSourceVideo().getId(),
				clip.getClipCandidate() != null ? clip.getClipCandidate().getId() : null,
				clip.getStartMs(),
				clip.getEndMs(),
				clip.getStoragePath(),
				clip.getStatus(),
				clip.getCreatedAt()
		);
	}
}