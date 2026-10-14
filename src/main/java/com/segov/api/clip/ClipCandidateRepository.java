package com.segov.api.clip;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClipCandidateRepository extends JpaRepository<ClipCandidate, Long> {

	List<ClipCandidate> findBySourceVideoIdOrderByStartMsAsc(Long sourceVideoId);

	@Modifying
	@Query("""
			delete from ClipCandidate candidate
			where candidate.sourceVideo.id = :sourceVideoId
			and not exists (select 1 from Clip clip where clip.clipCandidate = candidate)
			""")
	void deleteUnreferencedBySourceVideoId(@Param("sourceVideoId") Long sourceVideoId);
}