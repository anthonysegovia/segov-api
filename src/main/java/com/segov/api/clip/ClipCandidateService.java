package com.segov.api.clip;

import java.util.ArrayList;
import java.util.List;

import com.segov.api.clip.analysis.AiClipSuggestion;
import com.segov.api.clip.analysis.ClipAnalysisInput;
import com.segov.api.clip.analysis.ClipAnalysisService;
import com.segov.api.clip.analysis.TranscriptSegmentInput;
import com.segov.api.transcript.Transcript;
import com.segov.api.transcript.TranscriptSegment;
import com.segov.api.transcript.TranscriptSegmentRepository;
import com.segov.api.transcript.TranscriptService;
import com.segov.api.video.SourceVideo;
import com.segov.api.video.SourceVideoNotFoundException;
import com.segov.api.video.SourceVideoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClipCandidateService {

	private final SourceVideoRepository sourceVideoRepository;
	private final TranscriptService transcriptService;
	private final TranscriptSegmentRepository transcriptSegmentRepository;
	private final ClipAnalysisService clipAnalysisService;
	private final ClipCandidateGenerator clipCandidateGenerator;
	private final ClipCandidateRepository clipCandidateRepository;

	public ClipCandidateService(
			SourceVideoRepository sourceVideoRepository,
			TranscriptService transcriptService,
			TranscriptSegmentRepository transcriptSegmentRepository,
			ClipAnalysisService clipAnalysisService,
			ClipCandidateGenerator clipCandidateGenerator,
			ClipCandidateRepository clipCandidateRepository
	) {
		this.sourceVideoRepository = sourceVideoRepository;
		this.transcriptService = transcriptService;
		this.transcriptSegmentRepository = transcriptSegmentRepository;
		this.clipAnalysisService = clipAnalysisService;
		this.clipCandidateGenerator = clipCandidateGenerator;
		this.clipCandidateRepository = clipCandidateRepository;
	}

	@Transactional
	public List<ClipCandidate> generateForVideo(Long videoId) {
		SourceVideo sourceVideo = sourceVideoRepository.findById(videoId)
				.orElseThrow(() -> new SourceVideoNotFoundException(videoId));
		clipCandidateRepository.deleteUnreferencedBySourceVideoId(videoId);
		Transcript transcript = transcriptService.getByVideoId(videoId);
		List<TranscriptSegment> segments = transcriptSegmentRepository
				.findByTranscriptIdOrderByStartMsAsc(transcript.getId());
		List<ClipCandidate> candidates = new ArrayList<>();
		ClipAnalysisInput input = new ClipAnalysisInput(
				videoId,
				transcript.getText(),
				segments.stream()
						.map(segment -> new TranscriptSegmentInput(segment.getStartMs(), segment.getEndMs(), segment.getText()))
						.toList()
		);
		List<AiClipSuggestion> suggestions = clipAnalysisService.analyze(input);

		if (suggestions != null && !suggestions.isEmpty()) {
			for (AiClipSuggestion suggestion : suggestions) {
				if (isValidSuggestion(suggestion)) {
					ClipCandidate candidate = new ClipCandidate();
					candidate.setSourceVideo(sourceVideo);
					candidate.setStartMs(suggestion.startMs());
					candidate.setEndMs(suggestion.endMs());
					candidate.setTitle(suggestion.title());
					candidate.setReason(suggestion.reason());
					candidate.setScore(suggestion.score());
					candidate.setGenerationSource(ClipCandidateGenerationSource.AI);
					candidates.add(clipCandidateRepository.save(candidate));
				}
			}

			if (!candidates.isEmpty()) {
				return candidates;
			}
		}

		for (ClipCandidateGenerationResult result : clipCandidateGenerator.generate(segments)) {
			ClipCandidate candidate = new ClipCandidate();
			candidate.setSourceVideo(sourceVideo);
			candidate.setStartMs(result.startMs());
			candidate.setEndMs(result.endMs());
			candidate.setTitle(result.title());
			candidate.setReason(result.reason());
			candidate.setScore(result.score());
			candidate.setGenerationSource(ClipCandidateGenerationSource.RULE_BASED);
			candidates.add(clipCandidateRepository.save(candidate));
		}

		return candidates;
	}

	private boolean isValidSuggestion(AiClipSuggestion suggestion) {
		return suggestion != null
				&& suggestion.startMs() != null
				&& suggestion.endMs() != null
				&& suggestion.startMs() >= 0
				&& suggestion.endMs() > suggestion.startMs()
				&& (suggestion.title() == null || suggestion.title().length() <= 150);
	}

	public List<ClipCandidate> getByVideoId(Long videoId) {
		if (!sourceVideoRepository.existsById(videoId)) {
			throw new SourceVideoNotFoundException(videoId);
		}

		return clipCandidateRepository.findBySourceVideoIdOrderByStartMsAsc(videoId);
	}
}