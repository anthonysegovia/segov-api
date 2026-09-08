package com.segov.api.audio;

import java.time.LocalDateTime;

import com.segov.api.video.SourceVideo;
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
@Table(name = "extracted_audio")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class ExtractedAudio {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "source_video_id", nullable = false)
	private SourceVideo sourceVideo;

	private String storagePath;

	private Integer sampleRate = 16000;

	private Integer channels = 1;

	@CreatedDate
	private LocalDateTime createdAt;

	@LastModifiedDate
	private LocalDateTime updatedAt;
}