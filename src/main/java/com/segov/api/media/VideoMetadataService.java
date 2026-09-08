package com.segov.api.media;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

@Service
public class VideoMetadataService {

	private final ObjectMapper objectMapper;

	public VideoMetadataService(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public VideoMetadata probe(String videoPath) {
		ProcessBuilder processBuilder = new ProcessBuilder(
				"ffprobe",
				"-v", "error",
				"-select_streams", "v:0",
				"-show_entries", "stream=width,height",
				"-show_entries", "format=duration,format_name",
				"-of", "json",
				videoPath
		);
		processBuilder.redirectErrorStream(true);

		try {
			Process process = processBuilder.start();
			String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
			int exitCode = process.waitFor();

			if (exitCode != 0) {
				throw new RuntimeException("ffprobe failed with exit code " + exitCode + ": " + output);
			}

			JsonNode root = objectMapper.readTree(output);
			JsonNode stream = root.path("streams").path(0);
			JsonNode format = root.path("format");

			return new VideoMetadata(
					getDouble(format, "duration"),
					getInteger(stream, "width"),
					getInteger(stream, "height"),
					getString(format, "format_name")
			);
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			throw new RuntimeException("Interrupted while probing video metadata", exception);
		} catch (IOException exception) {
			throw new RuntimeException("Failed to probe video metadata", exception);
		}
	}

	private Double getDouble(JsonNode node, String fieldName) {
		return node.hasNonNull(fieldName) ? node.get(fieldName).asDouble() : null;
	}

	private Integer getInteger(JsonNode node, String fieldName) {
		return node.hasNonNull(fieldName) ? node.get(fieldName).asInt() : null;
	}

	private String getString(JsonNode node, String fieldName) {
		return node.hasNonNull(fieldName) ? node.get(fieldName).asText() : null;
	}
}