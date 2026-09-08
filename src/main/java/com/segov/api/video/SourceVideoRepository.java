package com.segov.api.video;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SourceVideoRepository extends JpaRepository<SourceVideo, Long> {

	List<SourceVideo> findByProjectId(Long projectId);
}