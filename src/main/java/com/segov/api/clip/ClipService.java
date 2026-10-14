package com.segov.api.clip;

import java.util.List;
import java.util.Optional;

import com.segov.api.video.SourceVideo;
import org.springframework.stereotype.Service;

@Service
public class ClipService {

	private final ClipCandidateRepository clipCandidateRepository;
	private final ClipRepository clipRepository;
	private final ClipExtractionService clipExtractionService;

	public ClipService(
			ClipCandidateRepository clipCandidateRepository,
			ClipRepository clipRepository,
			ClipExtractionService clipExtractionService
	) {
		this.clipCandidateRepository = clipCandidateRepository;
		this.clipRepository = clipRepository;
		this.clipExtractionService = clipExtractionService;
	}

	public Clip generateFromCandidate(Long candidateId) {
		ClipCandidate candidate = clipCandidateRepository.findById(candidateId)
				.orElseThrow(() -> new ClipCandidateNotFoundException(candidateId));
		Optional<Clip> existingClip = clipRepository.findFirstByClipCandidateIdAndStatusInOrderByCreatedAtDesc(
				candidateId,
				List.of(ClipStatus.READY, ClipStatus.PROCESSING)
		);
		if (existingClip.isPresent()) {
			return existingClip.get();
		}

		SourceVideo sourceVideo = candidate.getSourceVideo();

		Clip clip = new Clip();
		clip.setSourceVideo(sourceVideo);
		clip.setClipCandidate(candidate);
		clip.setStartMs(candidate.getStartMs());
		clip.setEndMs(candidate.getEndMs());
		clip.setStatus(ClipStatus.PROCESSING);
		clip = clipRepository.save(clip);

		try {
			String storagePath = clipExtractionService.extract(
					sourceVideo.getStoragePath(),
					candidate.getStartMs(),
					candidate.getEndMs()
			);
			clip.setStoragePath(storagePath);
			clip.setStatus(ClipStatus.READY);
			return clipRepository.save(clip);
		} catch (RuntimeException exception) {
			clip.setStatus(ClipStatus.FAILED);
			clipRepository.save(clip);
			throw exception;
		}
	}

	public List<Clip> getByVideoId(Long videoId) {
		return clipRepository.findBySourceVideoIdOrderByCreatedAtDesc(videoId);
	}
}