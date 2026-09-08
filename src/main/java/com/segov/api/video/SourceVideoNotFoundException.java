package com.segov.api.video;

public class SourceVideoNotFoundException extends RuntimeException {

	public SourceVideoNotFoundException(Long id) {
		super("Source video not found with id: " + id);
	}
}