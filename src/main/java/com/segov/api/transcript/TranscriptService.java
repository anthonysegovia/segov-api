package com.segov.api.transcript;

import com.segov.api.video.SourceVideoNotFoundException;
import com.segov.api.video.SourceVideoRepository;
import org.springframework.stereotype.Service;

@Service
public class TranscriptService {

	private final TranscriptRepository transcriptRepository;
	private final SourceVideoRepository sourceVideoRepository;

	public TranscriptService(
			TranscriptRepository transcriptRepository,
			SourceVideoRepository sourceVideoRepository
	) {
		this.transcriptRepository = transcriptRepository;
		this.sourceVideoRepository = sourceVideoRepository;
	}

	public Transcript getByVideoId(Long videoId) {
		if (!sourceVideoRepository.existsById(videoId)) {
			throw new SourceVideoNotFoundException(videoId);
		}

		return transcriptRepository.findFirstByExtractedAudioSourceVideoIdOrderByCreatedAtDesc(videoId)
				.orElseThrow(() -> new TranscriptNotFoundException(videoId));
	}
}