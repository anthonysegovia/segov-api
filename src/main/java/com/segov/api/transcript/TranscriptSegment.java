package com.segov.api.transcript;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "transcript_segments")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class TranscriptSegment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "transcript_id", nullable = false)
	private Transcript transcript;

	@Column(nullable = false)
	private Long startMs;

	@Column(nullable = false)
	private Long endMs;

	@Column(columnDefinition = "TEXT", nullable = false)
	private String text;

	@CreatedDate
	private LocalDateTime createdAt;
}