package com.segov.api.transcript;

import java.util.List;

import com.segov.api.video.SourceVideoNotFoundException;
import com.segov.api.video.SourceVideoRepository;
import org.springframework.stereotype.Service;

@Service
public class TranscriptService {

	private final TranscriptRepository transcriptRepository;
	private final TranscriptSegmentRepository transcriptSegmentRepository;
	private final SourceVideoRepository sourceVideoRepository;

	public TranscriptService(
			TranscriptRepository transcriptRepository,
			TranscriptSegmentRepository transcriptSegmentRepository,
			SourceVideoRepository sourceVideoRepository
	) {
		this.transcriptRepository = transcriptRepository;
		this.transcriptSegmentRepository = transcriptSegmentRepository;
		this.sourceVideoRepository = sourceVideoRepository;
	}

	public Transcript getByVideoId(Long videoId) {
		if (!sourceVideoRepository.existsById(videoId)) {
			throw new SourceVideoNotFoundException(videoId);
		}

		return transcriptRepository.findFirstByExtractedAudioSourceVideoIdOrderByCreatedAtDesc(videoId)
				.orElseThrow(() -> new TranscriptNotFoundException(videoId));
	}

	public List<TranscriptSegment> getSegmentsByVideoId(Long videoId) {
		Transcript transcript = getByVideoId(videoId);
		return transcriptSegmentRepository.findByTranscriptIdOrderByStartMsAsc(transcript.getId());
	}
}