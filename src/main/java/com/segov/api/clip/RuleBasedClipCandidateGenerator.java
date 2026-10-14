package com.segov.api.clip;

import java.util.ArrayList;
import java.util.List;

import com.segov.api.transcript.TranscriptSegment;
import org.springframework.stereotype.Service;

@Service
public class RuleBasedClipCandidateGenerator implements ClipCandidateGenerator {

	private static final long TARGET_DURATION_MS = 20_000;
	private static final long MIN_DURATION_MS = 10_000;
	private static final long MAX_DURATION_MS = 35_000;
	private static final String REASON = "Rule-based candidate generated from consecutive transcript segments.";

	@Override
	public List<ClipCandidateGenerationResult> generate(List<TranscriptSegment> segments) {
		List<List<TranscriptSegment>> windows = new ArrayList<>();
		if (segments == null || segments.isEmpty()) {
			return List.of();
		}

		List<TranscriptSegment> window = new ArrayList<>();
		for (TranscriptSegment segment : segments) {
			if (!hasUsableContent(segment)) {
				continue;
			}

			if (!window.isEmpty() && duration(window.getFirst(), segment) > MAX_DURATION_MS) {
				addWindow(window, windows);
				window.clear();
			}

			window.add(segment);
			if (duration(window.getFirst(), window.getLast()) >= TARGET_DURATION_MS) {
				addWindow(window, windows);
				window.clear();
			}
		}

		addFinalWindow(window, windows);

		List<ClipCandidateGenerationResult> candidates = new ArrayList<>();
		for (List<TranscriptSegment> candidateWindow : windows) {
			addCandidate(candidateWindow, candidates);
		}
		return candidates;
	}

	private boolean hasUsableContent(TranscriptSegment segment) {
		return segment != null
				&& segment.getStartMs() != null
				&& segment.getEndMs() != null
				&& segment.getEndMs() >= segment.getStartMs()
				&& segment.getText() != null
				&& !segment.getText().isBlank();
	}

	private void addWindow(List<TranscriptSegment> window, List<List<TranscriptSegment>> windows) {
		if (!window.isEmpty()) {
			windows.add(new ArrayList<>(window));
		}
	}

	private void addFinalWindow(List<TranscriptSegment> window, List<List<TranscriptSegment>> windows) {
		if (window.isEmpty()) {
			return;
		}

		long duration = duration(window.getFirst(), window.getLast());
		if (duration >= MIN_DURATION_MS) {
			addWindow(window, windows);
			return;
		}

		if (!windows.isEmpty()) {
			List<TranscriptSegment> previousWindow = windows.getLast();
			if (duration(previousWindow.getFirst(), window.getLast()) <= MAX_DURATION_MS) {
				previousWindow.addAll(window);
			}
		}
	}

	private void addCandidate(List<TranscriptSegment> window, List<ClipCandidateGenerationResult> candidates) {
		if (window.isEmpty()) {
			return;
		}

		TranscriptSegment firstSegment = window.getFirst();
		TranscriptSegment lastSegment = window.getLast();
		long duration = duration(firstSegment, lastSegment);
		if (duration < MIN_DURATION_MS || duration > MAX_DURATION_MS) {
			return;
		}

		String text = window.stream()
				.map(TranscriptSegment::getText)
				.map(String::trim)
				.filter(segmentText -> !segmentText.isBlank())
				.reduce((first, second) -> first + " " + second)
				.orElse("");
		if (text.isBlank()) {
			return;
		}

		candidates.add(new ClipCandidateGenerationResult(
				firstSegment.getStartMs(),
				lastSegment.getEndMs(),
				text.substring(0, Math.min(text.length(), 80)),
				REASON,
			scoreFor(duration)
		));
	}

	private long duration(TranscriptSegment firstSegment, TranscriptSegment lastSegment) {
		return lastSegment.getEndMs() - firstSegment.getStartMs();
	}

	private double scoreFor(long durationMs) {
		return Math.max(0.0, 1.0 - Math.abs(durationMs - TARGET_DURATION_MS) / (double) (TARGET_DURATION_MS - MIN_DURATION_MS));
	}
}