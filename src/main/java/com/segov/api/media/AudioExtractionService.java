package com.segov.api.media;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class AudioExtractionService {

	public String extractAudio(String videoPath) {
		Path audioDirectory = Path.of("uploads", "audio");
		Path outputPath = audioDirectory.resolve(UUID.randomUUID() + ".wav");

		try {
			Files.createDirectories(audioDirectory);

			ProcessBuilder processBuilder = new ProcessBuilder(
					"ffmpeg",
					"-y",
					"-i", videoPath,
					"-vn",
					"-ac", "1",
					"-ar", "16000",
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
			throw new RuntimeException("Interrupted while extracting audio", exception);
		} catch (IOException exception) {
			throw new RuntimeException("Failed to extract audio", exception);
		}
	}
}