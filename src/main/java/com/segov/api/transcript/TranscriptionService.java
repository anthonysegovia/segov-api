package com.segov.api.transcript;

public interface TranscriptionService {

	TranscriptionResult transcribe(String audioPath);
}