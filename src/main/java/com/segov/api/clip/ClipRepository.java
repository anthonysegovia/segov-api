package com.segov.api.clip;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ClipRepository extends JpaRepository<Clip, Long> {

	List<Clip> findBySourceVideoIdOrderByCreatedAtDesc(Long sourceVideoId);

	Optional<Clip> findFirstByClipCandidateIdAndStatusInOrderByCreatedAtDesc(
			Long clipCandidateId,
			List<ClipStatus> statuses
	);
}