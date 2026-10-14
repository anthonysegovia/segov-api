package com.segov.api.clip;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class ClipExtractionService {

	public String extract(String sourceVideoPath, Long startMs, Long endMs) {
		validateInput(sourceVideoPath, startMs, endMs);

		Path clipDirectory = Path.of("uploads", "clips");
		Path outputPath = clipDirectory.resolve(UUID.randomUUID() + ".mp4");
		long durationMs = endMs - startMs;

		try {
			Files.createDirectories(clipDirectory);

			ProcessBuilder processBuilder = new ProcessBuilder(
					"ffmpeg",
					"-y",
					"-ss", toSeconds(startMs),
					"-i", sourceVideoPath,
					"-t", toSeconds(durationMs),
					"-c:v", "libx264",
					"-c:a", "aac",
					"-movflags", "+faststart",
					outputPath.toString()
			);
			processBuilder.redirectErrorStream(true);

			Process process = processBuilder.start();
			String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
			int exitCode = process.waitFor();

			if (exitCode != 0) {
				throw new RuntimeException("ffmpeg failed with exit code " + exitCode + ": " + output);
			}

			return outputPath.toString();
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			throw new RuntimeException("Interrupted while extracting clip", exception);
		} catch (IOException exception) {
			throw new RuntimeException("Failed to extract clip", exception);
		}
	}

	private void validateInput(String sourceVideoPath, Long startMs, Long endMs) {
		if (sourceVideoPath == null || sourceVideoPath.isBlank()) {
			throw new IllegalArgumentException("Source video path is required");
		}
		if (startMs == null || startMs < 0) {
			throw new IllegalArgumentException("Start time must be zero or greater");
		}
		if (endMs == null || endMs <= startMs) {
			throw new IllegalArgumentException("End time must be greater than start time");
		}
	}

	private String toSeconds(long milliseconds) {
		return BigDecimal.valueOf(milliseconds).movePointLeft(3).stripTrailingZeros().toPlainString();
	}
}