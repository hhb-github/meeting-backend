package com.meeting.manager.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "meeting_action_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MeetingActionItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_record_id", nullable = false)
    private MeetingRecord meetingRecord;
    
    @Column(name = "task_description", nullable = false, columnDefinition = "TEXT")
    private String taskDescription;
    
    @Column(name = "responsible_person", length = 100)
    private String responsiblePerson;
    
    @Column(name = "assigned_to", length = 100)
    private String assignedTo;
    
    @Column(name = "department", length = 100)
    private String department;
    
    @Column(name = "priority")
    private String priority;
    
    @Column(name = "due_date")
    private LocalDateTime dueDate;
    
    @Column(name = "status")
    private String status;
    
    @Column(name = "completion_date")
    private LocalDateTime completionDate;
    
    @Column(name = "progress_notes", columnDefinition = "TEXT")
    private String progressNotes;
    
    @Column(name = "completion_notes", columnDefinition = "TEXT")
    private String completionNotes;
    
    @Column(name = "related_topic_id")
    private Long relatedTopicId;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = "PENDING";
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}