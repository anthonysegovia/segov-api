package com.segov.api.transcript;

public class TranscriptNotFoundException extends RuntimeException {

	public TranscriptNotFoundException(Long videoId) {
		super("Transcript not found for source video with id: " + videoId);
	}
}