package com.segov.api.processing;

import com.segov.api.audio.ExtractedAudio;
import com.segov.api.audio.ExtractedAudioRepository;
import com.segov.api.media.AudioExtractionService;
import com.segov.api.media.VideoMetadata;
import com.segov.api.media.VideoMetadataService;
import com.segov.api.transcript.Transcript;
import com.segov.api.transcript.TranscriptRepository;
import com.segov.api.transcript.TranscriptionResult;
import com.segov.api.transcript.TranscriptionService;
import com.segov.api.video.SourceVideo;
import com.segov.api.video.SourceVideoRepository;
import com.segov.api.video.SourceVideoStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class ProcessingWorkerService {

	private static final Logger logger = LoggerFactory.getLogger(ProcessingWorkerService.class);

	private final ProcessingJobRepository processingJobRepository;
	private final SourceVideoRepository sourceVideoRepository;
	private final ExtractedAudioRepository extractedAudioRepository;
	private final TranscriptRepository transcriptRepository;
	private final VideoMetadataService videoMetadataService;
	private final AudioExtractionService audioExtractionService;
	private final TranscriptionService transcriptionService;

	public ProcessingWorkerService(
			ProcessingJobRepository processingJobRepository,
			SourceVideoRepository sourceVideoRepository,
			ExtractedAudioRepository extractedAudioRepository,
			TranscriptRepository transcriptRepository,
			VideoMetadataService videoMetadataService,
			AudioExtractionService audioExtractionService,
			TranscriptionService transcriptionService
	) {
		this.processingJobRepository = processingJobRepository;
		this.sourceVideoRepository = sourceVideoRepository;
		this.extractedAudioRepository = extractedAudioRepository;
		this.transcriptRepository = transcriptRepository;
		this.videoMetadataService = videoMetadataService;
		this.audioExtractionService = audioExtractionService;
		this.transcriptionService = transcriptionService;
	}

	@Async
	public void processAsync(Long jobId) {
		ProcessingJob processingJob = processingJobRepository.findById(jobId)
				.orElseThrow(() -> new ProcessingJobNotFoundException(jobId));

		try {
			SourceVideo sourceVideo = processingJob.getSourceVideo();
			VideoMetadata videoMetadata = videoMetadataService.probe(sourceVideo.getStoragePath());
			sourceVideo.setDurationSeconds(videoMetadata.durationSeconds());
			sourceVideo.setWidth(videoMetadata.width());
			sourceVideo.setHeight(videoMetadata.height());
			sourceVideo.setFormatName(videoMetadata.formatName());
			sourceVideoRepository.save(sourceVideo);

			String audioPath = audioExtractionService.extractAudio(sourceVideo.getStoragePath());
			logger.info("Extracted audio for job {}: {}", jobId, audioPath);

			ExtractedAudio extractedAudio = new ExtractedAudio();
			extractedAudio.setSourceVideo(sourceVideo);
			extractedAudio.setStoragePath(audioPath);
			extractedAudio.setSampleRate(16000);
			extractedAudio.setChannels(1);
			extractedAudioRepository.save(extractedAudio);

			TranscriptionResult transcriptionResult = transcriptionService.transcribe(audioPath);
			Transcript transcript = new Transcript();
			transcript.setExtractedAudio(extractedAudio);
			transcript.setText(transcriptionResult.text());
			transcript.setLanguage(transcriptionResult.language());
			transcriptRepository.save(transcript);

			sourceVideo.setStatus(SourceVideoStatus.READY);
			sourceVideoRepository.save(sourceVideo);

			processingJob.setStatus(ProcessingJobStatus.COMPLETED);
			processingJobRepository.save(processingJob);
		} catch (RuntimeException exception) {
			SourceVideo sourceVideo = processingJob.getSourceVideo();
			sourceVideo.setStatus(SourceVideoStatus.FAILED);
			sourceVideoRepository.save(sourceVideo);

			processingJob.setStatus(ProcessingJobStatus.FAILED);
			processingJobRepository.save(processingJob);
			logger.error("Failed to process job {}: {}", jobId, exception.getMessage(), exception);
			throw exception;
		}
	}
}