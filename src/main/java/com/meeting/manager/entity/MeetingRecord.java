package com.meeting.manager.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "meeting_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MeetingRecord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "meeting_name", nullable = false, length = 255)
    private String meetingName;
    
    @Column(name = "meeting_topic", length = 500)
    private String meetingTopic;
    
    @Column(name = "meeting_date")
    private LocalDateTime meetingDate;
    
    @Column(name = "location", length = 255)
    private String location;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false)
    private SourceType sourceType;
    
    @Column(name = "original_file_name", nullable = false)
    private String originalFileName;
    
    @Column(name = "file_path", nullable = false)
    private String filePath;
    
    @Column(name = "file_size")
    private Long fileSize;
    
    @Column(name = "file_format")
    private String fileFormat;
    
    @Column(name = "transcription_text", columnDefinition = "TEXT")
    private String transcriptionText;
    
    @Column(name = "analysis_result", columnDefinition = "TEXT")
    private String analysisResult;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "processing_status")
    private ProcessingStatus processingStatus;
    
    @Column(name = "processing_error")
    private String processingError;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "processed_at")
    private LocalDateTime processedAt;
    
    @Column(name = "language")
    private String language;
    
    @Column(name = "duration")
    private Integer duration;
    
    @OneToMany(mappedBy = "meetingRecord", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MeetingParticipant> participants;
    
    @OneToMany(mappedBy = "meetingRecord", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MeetingTopic> topics;
    
    @OneToMany(mappedBy = "meetingRecord", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MeetingDecision> decisions;
    
    @OneToMany(mappedBy = "meetingRecord", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MeetingActionItem> actionItems;
    
    @OneToMany(mappedBy = "meetingRecord", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MeetingFollowUp> followUps;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (processingStatus == null) {
            processingStatus = ProcessingStatus.PENDING;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum SourceType {
        AUDIO, DOCUMENT
    }
    
    public enum ProcessingStatus {
        PENDING, PROCESSING, COMPLETED, FAILED
    }
}