package com.segov.api.transcript;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TranscriptSegmentRepository extends JpaRepository<TranscriptSegment, Long> {

	List<TranscriptSegment> findByTranscriptIdOrderByStartMsAsc(Long transcriptId);
}