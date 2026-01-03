package com.meeting.manager.entity;

import com.meeting.manager.enums.ActionPriority;
import com.meeting.manager.enums.ActionItemStatus;
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
    
    @Enumerated(EnumType.STRING)
    @Column(name = "priority")
    private ActionPriority priority;
    
    @Column(name = "due_date")
    private LocalDateTime dueDate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ActionItemStatus status;
    
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
            status = ActionItemStatus.PENDING;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}