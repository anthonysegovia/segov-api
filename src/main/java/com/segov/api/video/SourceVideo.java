package com.segov.api.video;

import java.time.LocalDateTime;

import com.segov.api.project.Project;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "source_videos")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class SourceVideo {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "project_id", nullable = false)
	private Project project;

	private String originalFilename;

	private String storagePath;

	private Double durationSeconds;

	private Integer width;

	private Integer height;

	private String formatName;

	@Enumerated(EnumType.STRING)
	private SourceVideoStatus status = SourceVideoStatus.UPLOADED;

	@CreatedDate
	private LocalDateTime createdAt;

	@LastModifiedDate
	private LocalDateTime updatedAt;
}