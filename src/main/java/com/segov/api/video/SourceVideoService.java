package com.segov.api.video;

import java.util.List;
import java.util.Set;

import com.segov.api.media.VideoMetadata;
import com.segov.api.media.VideoMetadataService;
import com.segov.api.project.Project;
import com.segov.api.project.ProjectNotFoundException;
import com.segov.api.project.ProjectRepository;
import com.segov.api.storage.VideoStorageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class SourceVideoService {

	private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
			"video/mp4",
			"video/quicktime",
			"video/webm"
	);

	private final SourceVideoRepository sourceVideoRepository;
	private final ProjectRepository projectRepository;
	private final VideoStorageService videoStorageService;
	private final VideoMetadataService videoMetadataService;

	public SourceVideoService(
			SourceVideoRepository sourceVideoRepository,
			ProjectRepository projectRepository,
			VideoStorageService videoStorageService,
			VideoMetadataService videoMetadataService
	) {
		this.sourceVideoRepository = sourceVideoRepository;
		this.projectRepository = projectRepository;
		this.videoStorageService = videoStorageService;
		this.videoMetadataService = videoMetadataService;
	}

	public SourceVideo uploadVideo(Long projectId, MultipartFile file) {
		validateVideoFile(file);

		Project project = projectRepository.findById(projectId)
				.orElseThrow(() -> new ProjectNotFoundException(projectId));
		String storagePath = videoStorageService.store(file);

		SourceVideo sourceVideo = new SourceVideo();
		sourceVideo.setProject(project);
		sourceVideo.setOriginalFilename(file.getOriginalFilename());
		sourceVideo.setStoragePath(storagePath);

		try {
			return sourceVideoRepository.save(sourceVideo);
		} catch (RuntimeException exception) {
			try {
				videoStorageService.delete(storagePath);
			} catch (RuntimeException cleanupException) {
				exception.addSuppressed(cleanupException);
			}

			throw exception;
		}
	}

	public List<SourceVideo> getVideosByProjectId(Long projectId) {
		if (!projectRepository.existsById(projectId)) {
			throw new ProjectNotFoundException(projectId);
		}

		return sourceVideoRepository.findByProjectId(projectId);
	}

	public SourceVideo getVideoById(Long id) {
		return sourceVideoRepository.findById(id)
				.orElseThrow(() -> new SourceVideoNotFoundException(id));
	}

	public VideoMetadata getVideoMetadata(Long id) {
		SourceVideo sourceVideo = sourceVideoRepository.findById(id)
				.orElseThrow(() -> new SourceVideoNotFoundException(id));

		return videoMetadataService.probe(sourceVideo.getStoragePath());
	}

	private void validateVideoFile(MultipartFile file) {
		if (file == null) {
			throw new InvalidVideoFileException("Video file is required");
		}

		if (file.isEmpty()) {
			throw new InvalidVideoFileException("Video file must not be empty");
		}

		if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
			throw new InvalidVideoFileException("Unsupported video file type");
		}
	}
}