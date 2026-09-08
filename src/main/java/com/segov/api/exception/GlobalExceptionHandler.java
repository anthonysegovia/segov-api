package com.segov.api.exception;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import com.segov.api.project.ProjectNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ProjectNotFoundException.class)
	public ResponseEntity<Map<String, Object>> handleProjectNotFound(
			ProjectNotFoundException exception,
			HttpServletRequest request
	) {
		Map<String, Object> body = Map.of(
				"timestamp", LocalDateTime.now(),
				"status", HttpStatus.NOT_FOUND.value(),
				"error", HttpStatus.NOT_FOUND.getReasonPhrase(),
				"message", exception.getMessage(),
				"path", request.getRequestURI()
		);

		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, Object>> handleValidationException(
			MethodArgumentNotValidException exception,
			HttpServletRequest request
	) {
		Map<String, String> errors = new LinkedHashMap<>();
		exception.getBindingResult().getFieldErrors().forEach(error ->
				errors.putIfAbsent(error.getField(), error.getDefaultMessage())
		);

		Map<String, Object> body = Map.of(
				"timestamp", LocalDateTime.now(),
				"status", HttpStatus.BAD_REQUEST.value(),
				"error", HttpStatus.BAD_REQUEST.getReasonPhrase(),
				"message", "Validation failed",
				"errors", errors,
				"path", request.getRequestURI()
		);

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
	}
}