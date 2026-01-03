package com.meeting.manager.entity;

import com.meeting.manager.enums.FollowUpStatus;
import com.meeting.manager.enums.FollowUpPriority;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "meeting_follow_ups")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MeetingFollowUp {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_record_id", nullable = false)
    private MeetingRecord meetingRecord;
    
    @Column(name = "issue_title", nullable = false, length = 255)
    private String issueTitle;
    
    @Column(name = "issue_description", columnDefinition = "TEXT")
    private String issueDescription;
    
    @Column(name = "unresolved_reason", columnDefinition = "TEXT")
    private String unresolvedReason;
    
    @Column(name = "follow_up_plan", columnDefinition = "TEXT")
    private String followUpPlan;
    
    @Column(name = "next_meeting_date")
    private LocalDateTime nextMeetingDate;
    
    @Column(name = "responsible_person", length = 100)
    private String responsiblePerson;
    
    @Column(name = "department", length = 100)
    private String department;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "priority")
    private FollowUpPriority priority;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private FollowUpStatus status;
    
    @Column(name = "target_resolution_date")
    private LocalDateTime targetResolutionDate;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = FollowUpStatus.OPEN;
        }
        if (priority == null) {
            priority = FollowUpPriority.MEDIUM;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}