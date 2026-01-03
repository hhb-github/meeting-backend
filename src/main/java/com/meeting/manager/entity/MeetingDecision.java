package com.meeting.manager.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "meeting_decisions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MeetingDecision {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_record_id", nullable = false)
    private MeetingRecord meetingRecord;
    
    @Column(name = "decision_title", nullable = false, length = 255)
    private String decisionTitle;
    
    @Column(name = "decision_description", columnDefinition = "TEXT")
    private String decisionDescription;
    
    @Column(name = "decision_content", columnDefinition = "TEXT")
    private String decisionContent;
    
    @Column(name = "decision_type")
    private String decisionType;
    
    @Column(name = "consensus_level")
    private String consensusLevel;
    
    @Column(name = "approval_required")
    private Boolean approvalRequired;
    
    @Column(name = "status")
    private String status;
    
    @Column(name = "decision_maker")
    private String decisionMaker;
    
    @Column(name = "implementation_plan", columnDefinition = "TEXT")
    private String implementationPlan;
    
    @Column(name = "deadline")
    private LocalDateTime deadline;
    
    @Column(name = "priority")
    private String priority;
    
    @Column(name = "decision_makers", columnDefinition = "TEXT")
    private String decisionMakers;
    
    @Column(name = "decision_date")
    private LocalDateTime decisionDate;
    
    @Column(name = "effective_date")
    private LocalDateTime effectiveDate;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}