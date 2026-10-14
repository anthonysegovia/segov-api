package com.segov.api.transcript;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class LocalWhisperTranscriptionService implements TranscriptionService {

	private final String whisperExecutable;
	private final String whisperModel;
	private final String language;
	private final ObjectMapper objectMapper;

	public LocalWhisperTranscriptionService(
			@Value("${segov.transcription.whisper.executable}") String whisperExecutable,
			@Value("${segov.transcription.whisper.model}") String whisperModel,
			@Value("${segov.transcription.whisper.language:auto}") String language,
			ObjectMapper objectMapper
	) {
		this.whisperExecutable = whisperExecutable;
		this.whisperModel = whisperModel;
		this.language = language;
		this.objectMapper = objectMapper;
	}

	@Override
	public TranscriptionResult transcribe(String audioPath) {
		Path transcriptDirectory = Path.of("uploads", "transcripts");
		Path outputPrefix = transcriptDirectory.resolve(UUID.randomUUID().toString());
		Path outputFile = Path.of(outputPrefix + ".json");

		try {
			Files.createDirectories(transcriptDirectory);

			List<String> command = new ArrayList<>(List.of(
					whisperExecutable,
					"-m", whisperModel,
					"-f", audioPath,
					"-oj",
					"-of", outputPrefix.toString()
			));
			if (hasFixedLanguage()) {
				command.add("-l");
				command.add(language);
			}

			ProcessBuilder processBuilder = new ProcessBuilder(command);
			processBuilder.redirectErrorStream(true);

			Process process = processBuilder.start();
			String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
			int exitCode = process.waitFor();

			if (exitCode != 0) {
				throw new RuntimeException("whisper-cli failed with exit code " + exitCode + ": " + output);
			}

			JsonNode root = objectMapper.readTree(Files.readString(outputFile, StandardCharsets.UTF_8));
			List<TranscriptionSegmentResult> segments = new ArrayList<>();
			for (JsonNode transcription : root.path("transcription")) {
				String text = transcription.path("text").asText("").trim();
				if (!text.isBlank()) {
					JsonNode offsets = transcription.path("offsets");
					segments.add(new TranscriptionSegmentResult(
							offsets.path("from").asLong(),
							offsets.path("to").asLong(),
							text
					));
				}
			}

			String detectedLanguage = root.path("result").path("language").asText(null);
			return new TranscriptionResult(
					segments.stream().map(TranscriptionSegmentResult::text).reduce((first, second) -> first + "\n" + second).orElse(""),
					detectedLanguage != null ? detectedLanguage : (hasFixedLanguage() ? language : null),
					segments
			);
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			throw new RuntimeException("Interrupted while transcribing audio", exception);
		} catch (IOException exception) {
			throw new RuntimeException("Failed to transcribe audio", exception);
		}
	}

	private boolean hasFixedLanguage() {
		return language != null && !language.isBlank() && !"auto".equalsIgnoreCase(language);
	}
}