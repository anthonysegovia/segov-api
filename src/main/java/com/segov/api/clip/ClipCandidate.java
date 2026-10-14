package com.segov.api.clip;

import java.time.LocalDateTime;

import com.segov.api.video.SourceVideo;
import jakarta.persistence.Column;
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
@Table(name = "clip_candidates")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class ClipCandidate {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "source_video_id", nullable = false)
	private SourceVideo sourceVideo;

	@Column(nullable = false)
	private Long startMs;

	@Column(nullable = false)
	private Long endMs;

	@Column(length = 150)
	private String title;

	@Column(columnDefinition = "TEXT")
	private String reason;

	private Double score;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, columnDefinition = "varchar(255) default 'RULE_BASED'")
	private ClipCandidateGenerationSource generationSource = ClipCandidateGenerationSource.RULE_BASED;

	@Enumerated(EnumType.STRING)
	private ClipCandidateStatus status = ClipCandidateStatus.SUGGESTED;

	@CreatedDate
	private LocalDateTime createdAt;

	@LastModifiedDate
	private LocalDateTime updatedAt;
}