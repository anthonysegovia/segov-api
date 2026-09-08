package com.segov.api.video;

public class InvalidVideoFileException extends RuntimeException {

	public InvalidVideoFileException(String message) {
		super(message);
	}
}