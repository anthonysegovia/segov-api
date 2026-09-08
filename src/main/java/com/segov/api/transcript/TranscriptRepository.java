package com.segov.api.transcript;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TranscriptRepository extends JpaRepository<Transcript, Long> {

	Optional<Transcript> findFirstByExtractedAudioSourceVideoIdOrderByCreatedAtDesc(Long sourceVideoId);
}