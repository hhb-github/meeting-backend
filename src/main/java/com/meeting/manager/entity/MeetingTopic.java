package com.meeting.manager.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "meeting_topics")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MeetingTopic {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_record_id", nullable = false)
    private MeetingRecord meetingRecord;
    
    @Column(name = "topic_title", nullable = false, length = 255)
    private String topicTitle;
    
    @Column(name = "topic_description", columnDefinition = "TEXT")
    private String topicDescription;
    
    @Column(name = "discussion_points", columnDefinition = "TEXT")
    private String discussionPoints;
    
    @Column(name = "speaker_views", columnDefinition = "TEXT")
    private String speakerViews;
    
    @Column(name = "topic_order")
    private Integer topicOrder;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}