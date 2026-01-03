package com.meeting.manager.service;

import com.meeting.manager.service.MeetingAnalysisService;
import com.meeting.manager.dto.StructuredMeetingSummary;
import com.meeting.manager.entity.*;
import com.meeting.manager.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class FileProcessingService {

    @Autowired
    private MeetingRecordRepository meetingRecordRepository;

    @Autowired
    private MeetingParticipantRepository participantRepository;

    @Autowired
    private MeetingTopicRepository topicRepository;

    @Autowired
    private MeetingDecisionRepository decisionRepository;

    @Autowired
    private MeetingActionItemRepository actionItemRepository;

    @Autowired
    private MeetingFollowUpRepository followUpRepository;

    @Autowired
    private MeetingAnalysisService analysisService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private SpeechToTextService speechToTextService;

    public FileStorageService getFileStorageService() {
        return fileStorageService;
    }

    @Async
    public CompletableFuture<Void> processFileAsync(Long meetingRecordId) {
        try {
            log.info("开始异步处理文件: {}", meetingRecordId);
            processFile(meetingRecordId);
        } catch (Exception e) {
            log.error("处理文件失败: {}", meetingRecordId, e);
            updateMeetingStatus(meetingRecordId, MeetingRecord.ProcessingStatus.FAILED, e.getMessage());
        }
        return CompletableFuture.completedFuture(null);
    }

    @Transactional
    public void processFile(Long meetingRecordId) throws Exception {
        MeetingRecord meetingRecord = meetingRecordRepository.findById(meetingRecordId)
                .orElseThrow(() -> new RuntimeException("会议记录不存在"));

        try {
            updateMeetingStatus(meetingRecordId, MeetingRecord.ProcessingStatus.PROCESSING, null);

            String transcriptionText = null;

            if (meetingRecord.getSourceType() == MeetingRecord.SourceType.AUDIO) {
                log.info("开始语音转文字处理: {}", meetingRecord.getFilePath());
                transcriptionText = speechToTextService.convertSpeechToText(meetingRecord.getFilePath());
            } else {
                log.info("开始文档内容提取: {}", meetingRecord.getFilePath());
                transcriptionText = fileStorageService.extractTextFromDocument(meetingRecord.getFilePath());
            }

            meetingRecord.setTranscriptionText(transcriptionText);
            meetingRecordRepository.save(meetingRecord);

            log.info("开始文本分析处理");
            StructuredMeetingSummary summary = analysisService.analyzeMeetingContent(transcriptionText);
            
            // 将结构化纪要转换为JSON字符串保存
            String analysisResult = objectMapper.writeValueAsString(summary);
            meetingRecord.setAnalysisResult(analysisResult);

            log.info("生成结构化纪要");
            saveStructuredData(meetingRecord, summary);

            meetingRecord.setProcessingStatus(MeetingRecord.ProcessingStatus.COMPLETED);
            meetingRecord.setProcessedAt(LocalDateTime.now());
            meetingRecordRepository.save(meetingRecord);

            log.info("文件处理完成: {}", meetingRecordId);

        } catch (Exception e) {
            log.error("文件处理失败", e);
            updateMeetingStatus(meetingRecordId, MeetingRecord.ProcessingStatus.FAILED, e.getMessage());
            throw e;
        }
    }

    private void updateMeetingStatus(Long meetingRecordId, MeetingRecord.ProcessingStatus status, String error) {
        MeetingRecord meetingRecord = meetingRecordRepository.findById(meetingRecordId).orElse(null);
        if (meetingRecord != null) {
            meetingRecord.setProcessingStatus(status);
            meetingRecord.setProcessingError(error);
            meetingRecord.setUpdatedAt(LocalDateTime.now());
            meetingRecordRepository.save(meetingRecord);
        }
    }

    private StructuredMeetingSummary parseStructuredSummary(String jsonResult) {
        try {
            return objectMapper.readValue(jsonResult, StructuredMeetingSummary.class);
        } catch (Exception e) {
            log.error("解析结构化结果失败", e);
            throw new RuntimeException("解析结构化结果失败", e);
        }
    }

    private void saveStructuredData(MeetingRecord meetingRecord, StructuredMeetingSummary summary) {
        if (summary.getBasicInfo() != null) {
            saveBasicInfo(meetingRecord, summary.getBasicInfo());
        }

        if (summary.getDiscussionPoints() != null) {
            saveTopics(meetingRecord, summary.getDiscussionPoints());
        }

        if (summary.getDecisions() != null) {
            saveDecisions(meetingRecord, summary.getDecisions());
        }

        if (summary.getActionItems() != null) {
            saveActionItems(meetingRecord, summary.getActionItems());
        }

        if (summary.getFollowUps() != null) {
            saveFollowUps(meetingRecord, summary.getFollowUps());
        }
    }

    private void saveBasicInfo(MeetingRecord meetingRecord, StructuredMeetingSummary.MeetingBasicInfo basicInfo) {
        if (basicInfo.getMeetingName() != null) {
            meetingRecord.setMeetingName(basicInfo.getMeetingName());
        }
        if (basicInfo.getMeetingTopic() != null) {
            meetingRecord.setMeetingTopic(basicInfo.getMeetingTopic());
        }
        if (basicInfo.getLocation() != null) {
            meetingRecord.setLocation(basicInfo.getLocation());
        }

        if (basicInfo.getParticipants() != null && !basicInfo.getParticipants().isEmpty()) {
            for (String participantName : basicInfo.getParticipants()) {
                MeetingParticipant participant = MeetingParticipant.builder()
                        .meetingRecord(meetingRecord)
                        .name(participantName)
                        .attendanceStatus("ATTENDED")
                        .build();
                participantRepository.save(participant);
            }
        }
    }

    private void saveTopics(MeetingRecord meetingRecord, List<StructuredMeetingSummary.DiscussionPoint> discussionPoints) {
        for (int i = 0; i < discussionPoints.size(); i++) {
            StructuredMeetingSummary.DiscussionPoint point = discussionPoints.get(i);
            MeetingTopic topic = MeetingTopic.builder()
                    .meetingRecord(meetingRecord)
                    .topicTitle(point.getTopicTitle())
                    .topicDescription(point.getTopicDescription())
                    .discussionPoints(formatList(point.getDiscussionPoints()))
                    .speakerViews(formatSpeakerViews(point.getSpeakerViews()))
                    .topicOrder(point.getTopicOrder() != null ? point.getTopicOrder() : i + 1)
                    .build();
            topicRepository.save(topic);
        }
    }

    private void saveDecisions(MeetingRecord meetingRecord, List<StructuredMeetingSummary.DecisionItem> decisions) {
        for (StructuredMeetingSummary.DecisionItem decision : decisions) {
            MeetingDecision meetingDecision = MeetingDecision.builder()
                    .meetingRecord(meetingRecord)
                    .decisionTitle(decision.getDecisionTitle())
                    .decisionContent(decision.getDecisionContent())
                    .decisionType(decision.getDecisionType())
                    .consensusLevel(decision.getConsensusLevel())
                    .decisionMakers(formatList(decision.getDecisionMakers()))
                    .decisionDate(decision.getDecisionDate())
                    .effectiveDate(decision.getEffectiveDate())
                    .build();
            decisionRepository.save(meetingDecision);
        }
    }

    private void saveActionItems(MeetingRecord meetingRecord, List<StructuredMeetingSummary.ActionItem> actionItems) {
        for (StructuredMeetingSummary.ActionItem actionItem : actionItems) {
            MeetingActionItem item = MeetingActionItem.builder()
                    .meetingRecord(meetingRecord)
                    .taskDescription(actionItem.getTaskDescription())
                    .responsiblePerson(actionItem.getResponsiblePerson())
                    .department(actionItem.getDepartment())
                    .priority(parsePriority(actionItem.getPriority()))
                    .dueDate(actionItem.getDueDate())
                    .status(parseActionItemStatus(actionItem.getStatus()))
                    .progressNotes(actionItem.getProgressNotes())
                    .relatedTopicId(actionItem.getRelatedTopicId())
                    .build();
            actionItemRepository.save(item);
        }
    }

    private void saveFollowUps(MeetingRecord meetingRecord, List<StructuredMeetingSummary.FollowUpItem> followUps) {
        for (StructuredMeetingSummary.FollowUpItem followUp : followUps) {
            MeetingFollowUp item = MeetingFollowUp.builder()
                    .meetingRecord(meetingRecord)
                    .issueTitle(followUp.getIssueTitle())
                    .issueDescription(followUp.getIssueDescription())
                    .unresolvedReason(followUp.getUnresolvedReason())
                    .followUpPlan(followUp.getFollowUpPlan())
                    .nextMeetingDate(followUp.getNextMeetingDate())
                    .responsiblePerson(followUp.getResponsiblePerson())
                    .department(followUp.getDepartment())
                    .priority(parseFollowUpPriority(followUp.getPriority()))
                    .status(parseFollowUpStatus(followUp.getStatus()))
                    .targetResolutionDate(followUp.getTargetResolutionDate())
                    .build();
            followUpRepository.save(item);
        }
    }

    private String formatList(List<String> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return String.join(", ", list);
    }

    private String formatSpeakerViews(List<StructuredMeetingSummary.SpeakerView> views) {
        if (views == null || views.isEmpty()) {
            return null;
        }
        return views.stream()
                .map(view -> String.format("%s(%s): %s", view.getSpeakerName(), view.getRole(), view.getView()))
                .collect(java.util.stream.Collectors.joining("\n"));
    }

    private LocalDateTime parseLocalDateTime(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateString);
        } catch (Exception e) {
            log.warn("日期解析失败: {}", dateString);
            return null;
        }
    }

    private MeetingActionItem.ActionPriority parsePriority(String priorityString) {
        if (priorityString == null || priorityString.trim().isEmpty()) {
            return null;
        }
        try {
            return MeetingActionItem.ActionPriority.valueOf(priorityString.toUpperCase());
        } catch (Exception e) {
            log.warn("优先级解析失败: {}, 使用默认值MEDIUM", priorityString);
            return MeetingActionItem.ActionPriority.MEDIUM;
        }
    }

    private MeetingActionItem.ActionItemStatus parseActionItemStatus(String statusString) {
        if (statusString == null || statusString.trim().isEmpty()) {
            return null;
        }
        try {
            return MeetingActionItem.ActionItemStatus.valueOf(statusString.toUpperCase());
        } catch (Exception e) {
            log.warn("行动项状态解析失败: {}, 使用默认值PENDING", statusString);
            return MeetingActionItem.ActionItemStatus.PENDING;
        }
    }

    private MeetingFollowUp.FollowUpPriority parseFollowUpPriority(String priorityString) {
        if (priorityString == null || priorityString.trim().isEmpty()) {
            return null;
        }
        try {
            return MeetingFollowUp.FollowUpPriority.valueOf(priorityString.toUpperCase());
        } catch (Exception e) {
            log.warn("跟进优先级解析失败: {}, 使用默认值MEDIUM", priorityString);
            return MeetingFollowUp.FollowUpPriority.MEDIUM;
        }
    }

    private MeetingFollowUp.FollowUpStatus parseFollowUpStatus(String statusString) {
        if (statusString == null || statusString.trim().isEmpty()) {
            return null;
        }
        try {
            return MeetingFollowUp.FollowUpStatus.valueOf(statusString.toUpperCase());
        } catch (Exception e) {
            log.warn("跟进状态解析失败: {}, 使用默认值OPEN", statusString);
            return MeetingFollowUp.FollowUpStatus.OPEN;
        }
    }
}