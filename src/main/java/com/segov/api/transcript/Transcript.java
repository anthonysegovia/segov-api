package com.segov.api.transcript;

import java.time.LocalDateTime;

import com.segov.api.audio.ExtractedAudio;
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
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "transcripts")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class Transcript {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "extracted_audio_id", nullable = false)
	private ExtractedAudio extractedAudio;

	@Column(columnDefinition = "TEXT", nullable = false)
	private String text;

	@Column(nullable = true)
	private String language;

	@CreatedDate
	private LocalDateTime createdAt;

	@LastModifiedDate
	private LocalDateTime updatedAt;
}