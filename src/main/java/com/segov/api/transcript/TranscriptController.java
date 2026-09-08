package com.segov.api.transcript;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TranscriptController {

	private final TranscriptService transcriptService;

	public TranscriptController(TranscriptService transcriptService) {
		this.transcriptService = transcriptService;
	}

	@GetMapping("/api/videos/{videoId}/transcript")
	public ResponseEntity<TranscriptResponse> getTranscriptByVideoId(@PathVariable Long videoId) {
		Transcript transcript = transcriptService.getByVideoId(videoId);
		TranscriptResponse response = new TranscriptResponse(
				transcript.getId(),
				transcript.getText(),
				transcript.getLanguage(),
				transcript.getCreatedAt()
		);

		return ResponseEntity.ok(response);
	}
}