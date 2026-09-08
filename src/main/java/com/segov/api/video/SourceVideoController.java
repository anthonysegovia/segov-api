package com.segov.api.video;

import java.util.List;

import com.segov.api.media.VideoMetadata;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class SourceVideoController {

	private final SourceVideoService sourceVideoService;

	public SourceVideoController(SourceVideoService sourceVideoService) {
		this.sourceVideoService = sourceVideoService;
	}

	@PostMapping(path = "/api/projects/{projectId}/videos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<SourceVideo> uploadVideo(
			@PathVariable Long projectId,
			@RequestPart("file") MultipartFile file
	) {
		SourceVideo sourceVideo = sourceVideoService.uploadVideo(projectId, file);
		return ResponseEntity.status(HttpStatus.CREATED).body(sourceVideo);
	}

	@GetMapping("/api/projects/{projectId}/videos")
	public ResponseEntity<List<SourceVideo>> getVideosByProjectId(@PathVariable Long projectId) {
		return ResponseEntity.ok(sourceVideoService.getVideosByProjectId(projectId));
	}

	@GetMapping("/api/videos/{id}")
	public ResponseEntity<SourceVideo> getVideoById(@PathVariable Long id) {
		return ResponseEntity.ok(sourceVideoService.getVideoById(id));
	}

	@GetMapping("/api/videos/{id}/metadata")
	public ResponseEntity<VideoMetadata> getVideoMetadata(@PathVariable Long id) {
		return ResponseEntity.ok(sourceVideoService.getVideoMetadata(id));
	}
}