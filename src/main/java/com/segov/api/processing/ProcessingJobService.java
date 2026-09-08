package com.segov.api.processing;

import com.segov.api.video.SourceVideo;
import com.segov.api.video.SourceVideoNotFoundException;
import com.segov.api.video.SourceVideoRepository;
import com.segov.api.video.SourceVideoStatus;
import org.springframework.stereotype.Service;

@Service
public class ProcessingJobService {

	private final ProcessingJobRepository processingJobRepository;
	private final SourceVideoRepository sourceVideoRepository;
	private final ProcessingWorkerService processingWorkerService;

	public ProcessingJobService(
			ProcessingJobRepository processingJobRepository,
			SourceVideoRepository sourceVideoRepository,
			ProcessingWorkerService processingWorkerService
	) {
		this.processingJobRepository = processingJobRepository;
		this.sourceVideoRepository = sourceVideoRepository;
		this.processingWorkerService = processingWorkerService;
	}

	public ProcessingJob createProcessingJob(Long sourceVideoId) {
		SourceVideo sourceVideo = sourceVideoRepository.findById(sourceVideoId)
				.orElseThrow(() -> new SourceVideoNotFoundException(sourceVideoId));

		ProcessingJob processingJob = new ProcessingJob();
		processingJob.setSourceVideo(sourceVideo);

		return processingJobRepository.save(processingJob);
	}

	public ProcessingJob processJob(Long jobId) {
		ProcessingJob processingJob = processingJobRepository.findById(jobId)
				.orElseThrow(() -> new ProcessingJobNotFoundException(jobId));

		if (processingJob.getStatus() != ProcessingJobStatus.PENDING) {
			throw new InvalidProcessingJobStateException(jobId, processingJob.getStatus());
		}

		processingJob.setStatus(ProcessingJobStatus.PROCESSING);
		SourceVideo sourceVideo = processingJob.getSourceVideo();
		sourceVideo.setStatus(SourceVideoStatus.PROCESSING);
		sourceVideoRepository.save(sourceVideo);
		ProcessingJob savedProcessingJob = processingJobRepository.save(processingJob);
		processingWorkerService.processAsync(savedProcessingJob.getId());

		return savedProcessingJob;
	}

	public ProcessingJob getProcessingJobById(Long jobId) {
		return processingJobRepository.findById(jobId)
				.orElseThrow(() -> new ProcessingJobNotFoundException(jobId));
	}
}