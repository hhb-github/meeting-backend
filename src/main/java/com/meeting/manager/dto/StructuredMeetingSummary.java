package com.meeting.manager.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StructuredMeetingSummary {
    
    private MeetingBasicInfo basicInfo;
    private List<DiscussionPoint> discussionPoints;
    private List<DecisionItem> decisions;
    private List<ActionItem> actionItems;
    private List<FollowUpItem> followUps;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MeetingBasicInfo {
        private String meetingName;
        private String meetingTopic;
        private String meetingDate;
        private String location;
        private List<String> participants;
        private String sourceType;
        private String fileName;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DiscussionPoint {
        private String topicTitle;
        private String topicDescription;
        private List<String> discussionPoints;
        private List<SpeakerView> speakerViews;
        private Integer topicOrder;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SpeakerView {
        private String speakerName;
        private String role;
        private String view;
        private String department;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DecisionItem {
        private String decisionTitle;
        private String decisionContent;
        private String decisionType;
        private String consensusLevel;
        private List<String> decisionMakers;
        private LocalDateTime decisionDate;
        private LocalDateTime effectiveDate;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ActionItem {
        private String taskDescription;
        private String responsiblePerson;
        private String department;
        private String priority;
        private LocalDateTime dueDate;
        private String status;
        private String progressNotes;
        private Long relatedTopicId;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FollowUpItem {
        private String issueTitle;
        private String issueDescription;
        private String unresolvedReason;
        private String followUpPlan;
        private LocalDateTime nextMeetingDate;
        private String responsiblePerson;
        private String department;
        private String priority;
        private String status;
        private LocalDateTime targetResolutionDate;
    }
}