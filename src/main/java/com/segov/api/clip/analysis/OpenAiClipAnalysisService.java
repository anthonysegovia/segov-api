package com.segov.api.clip.analysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@Primary
public class OpenAiClipAnalysisService implements ClipAnalysisService {

	private static final Logger logger = LoggerFactory.getLogger(OpenAiClipAnalysisService.class);
	private static final String INSTRUCTIONS = """
			Segov selects the best short-form social media clips from video transcripts.
			Return between one and five strong clip candidates when the transcript contains enough meaningful
			content. For source videos under 60 seconds, actively consider multiple distinct clip-worthy moments;
			do not assume only one clip should be selected because the source video is short. If two or more
			clearly distinct useful moments exist, return multiple suggestions. Multiple suggestions may overlap
			when they represent different editorial angles or hooks. Prefer diversity of ideas over minor
			variations of the same range. A clip does not need to cover the entire source video, and do not create
			low-quality clips only to reach a target count. Each candidate needs a strong opening hook, must be
			understandable without excessive external context, and should contain an interesting, useful, funny,
			surprising, emotional, educational, or valuable idea. They must suit TikTok, Instagram Reels, or
			YouTube Shorts and should preferably be 15 to 45 seconds. Clips may span multiple transcript segments.
			Avoid incomplete thoughts and avoid starting or ending in the middle of a sentence when possible. Use
			only supplied transcript segment timestamp boundaries; never invent timestamps.
			""";
	private static final String CLIP_SCHEMA = """
			{
			  "type": "object",
			  "properties": {
			    "clips": {
			      "type": "array",
			      "items": {
			        "type": "object",
			        "properties": {
			          "startMs": { "type": "integer" },
			          "endMs": { "type": "integer" },
			          "title": { "type": "string" },
			          "reason": { "type": "string" },
			          "score": { "type": "number" }
			        },
			        "required": ["startMs", "endMs", "title", "reason", "score"],
			        "additionalProperties": false
			      }
			    }
			  },
			  "required": ["clips"],
			  "additionalProperties": false
			}
			""";

	private final RestClient restClient;
	private final ObjectMapper objectMapper;
	private final String baseUrl;
	private final String apiKey;
	private final String model;
	private final Map<String, Object> clipSchema;

	public OpenAiClipAnalysisService(
			ObjectMapper objectMapper,
			@Value("${segov.ai.openai.base-url:https://api.openai.com/v1}") String baseUrl,
			@Value("${segov.ai.openai.api-key:}") String apiKey,
			@Value("${segov.ai.openai.model:}") String model
	) {
		this.restClient = RestClient.builder().build();
		this.objectMapper = objectMapper;
		this.baseUrl = baseUrl;
		this.apiKey = apiKey;
		this.model = model;
		try {
			this.clipSchema = objectMapper.readValue(CLIP_SCHEMA, new TypeReference<>() {
			});
		} catch (Exception exception) {
			throw new IllegalStateException("Failed to initialize OpenAI clip analysis schema", exception);
		}
	}

	@Override
	public List<AiClipSuggestion> analyze(ClipAnalysisInput input) {
		if (!isConfigured() || !hasUsableInput(input)) {
			logger.warn("OpenAI clip analysis is not configured or has no usable transcript input");
			return Collections.emptyList();
		}

		logger.info("Requesting OpenAI clip analysis with model {} for {} transcript segments", model, input.segments().size());
		try {
			String response = restClient.post()
					.uri(responsesUrl())
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
					.contentType(MediaType.APPLICATION_JSON)
					.body(new OpenAiResponsesRequest(
							model,
							INSTRUCTIONS,
							buildInput(input),
							new TextConfiguration(new JsonSchemaFormat(
									"json_schema",
									"segov_clip_analysis",
									clipSchema,
									true
							))
					))
					.retrieve()
					.body(String.class);
			List<AiClipSuggestion> suggestions = parseSuggestions(response, input.segments());
			logger.info("Received {} valid OpenAI clip suggestions", suggestions.size());
			return suggestions;
		} catch (Exception exception) {
			logger.warn("OpenAI clip analysis failed: {}", exception.getMessage());
			return Collections.emptyList();
		}
	}

	private boolean isConfigured() {
		return !baseUrl.isBlank() && !apiKey.isBlank() && !model.isBlank();
	}

	private boolean hasUsableInput(ClipAnalysisInput input) {
		return input != null
				&& input.transcriptText() != null
				&& !input.transcriptText().isBlank()
				&& input.segments() != null
				&& !input.segments().isEmpty();
	}

	private String responsesUrl() {
		return baseUrl.endsWith("/") ? baseUrl + "responses" : baseUrl + "/responses";
	}

	private String buildInput(ClipAnalysisInput input) {
		StringBuilder content = new StringBuilder("Full transcript:\n");
		content.append(input.transcriptText()).append("\n\nTranscript segments:\n");
		for (TranscriptSegmentInput segment : input.segments()) {
			if (segment != null) {
				content.append("startMs=").append(segment.startMs())
						.append(", endMs=").append(segment.endMs())
						.append(", text=").append(segment.text())
						.append('\n');
			}
		}
		return content.toString();
	}

	private List<AiClipSuggestion> parseSuggestions(String response, List<TranscriptSegmentInput> segments) {
		try {
			String outputText = extractAssistantOutputText(response);
			if (outputText == null) {
				logger.warn("OpenAI response did not contain assistant output text");
				return Collections.emptyList();
			}

			OpenAiClipAnalysisResponse analysisResponse = objectMapper.readValue(outputText, OpenAiClipAnalysisResponse.class);
			Set<Long> startBoundaries = segments.stream()
					.filter(segment -> segment != null && segment.startMs() != null)
					.map(TranscriptSegmentInput::startMs)
					.collect(Collectors.toSet());
			Set<Long> endBoundaries = segments.stream()
					.filter(segment -> segment != null && segment.endMs() != null)
					.map(TranscriptSegmentInput::endMs)
					.collect(Collectors.toSet());
			List<AiClipSuggestion> suggestions = new ArrayList<>();
			if (analysisResponse.clips() != null) {
				for (OpenAiClipSuggestion suggestion : analysisResponse.clips()) {
					AiClipSuggestion validSuggestion = toValidSuggestion(suggestion, startBoundaries, endBoundaries);
					if (validSuggestion != null) {
						suggestions.add(validSuggestion);
					}
				}
			}
			Collections.sort(suggestions, Comparator.comparing(AiClipSuggestion::score).reversed());
			return suggestions;
		} catch (Exception exception) {
			logger.warn("Unable to parse OpenAI clip analysis response: {}", exception.getMessage());
			return Collections.emptyList();
		}
	}

	private String extractAssistantOutputText(String response) throws Exception {
		if (response == null || response.isBlank()) {
			return null;
		}

		JsonNode output = objectMapper.readTree(response).path("output");
		for (JsonNode outputItem : output) {
			if (!"assistant".equals(outputItem.path("role").asText())) {
				continue;
			}
			for (JsonNode content : outputItem.path("content")) {
				if ("output_text".equals(content.path("type").asText()) && content.hasNonNull("text")) {
					return content.get("text").asText();
				}
			}
		}
		return null;
	}

	private AiClipSuggestion toValidSuggestion(
			OpenAiClipSuggestion suggestion,
			Set<Long> startBoundaries,
			Set<Long> endBoundaries
	) {
		if (suggestion == null || suggestion.startMs() == null || suggestion.endMs() == null || suggestion.score() == null) {
			return null;
		}
		String title = suggestion.title() == null ? "" : suggestion.title().trim();
		String reason = suggestion.reason() == null ? "" : suggestion.reason().trim();
		if (suggestion.startMs() < 0 || suggestion.endMs() <= suggestion.startMs()
				|| !startBoundaries.contains(suggestion.startMs()) || !endBoundaries.contains(suggestion.endMs())
				|| title.isBlank() || reason.isBlank() || !Double.isFinite(suggestion.score())) {
			return null;
		}

		return new AiClipSuggestion(
				suggestion.startMs(),
				suggestion.endMs(),
				title,
				reason,
				Math.max(0.0, Math.min(1.0, suggestion.score()))
		);
	}

	private record OpenAiResponsesRequest(String model, String instructions, String input, TextConfiguration text) {
	}

	private record TextConfiguration(JsonSchemaFormat format) {
	}

	private record JsonSchemaFormat(String type, String name, Map<String, Object> schema, boolean strict) {
	}

	private record OpenAiClipAnalysisResponse(List<OpenAiClipSuggestion> clips) {
	}

	private record OpenAiClipSuggestion(Long startMs, Long endMs, String title, String reason, Double score) {
	}
}