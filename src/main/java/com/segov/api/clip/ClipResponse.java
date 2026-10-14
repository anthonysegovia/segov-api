package com.segov.api.clip;

import java.time.LocalDateTime;

public record ClipResponse(
		Long id,
		Long sourceVideoId,
		Long clipCandidateId,
		Long startMs,
		Long endMs,
		String storagePath,
		ClipStatus status,
		LocalDateTime createdAt
) {
}