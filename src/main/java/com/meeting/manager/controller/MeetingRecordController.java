package com.meeting.manager.controller;

import com.meeting.manager.dto.FileUploadRequest;
import com.meeting.manager.entity.*;
import com.meeting.manager.repository.*;
import com.meeting.manager.service.FileProcessingService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/meeting-records")
@Slf4j
public class MeetingRecordController {

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
    private FileProcessingService fileProcessingService;

    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("meetingName") String meetingName,
            @RequestParam(value = "meetingTopic", required = false) String meetingTopic,
            @RequestParam(value = "location", required = false) String location,
            @RequestParam(value = "language", required = false) String language,
            @RequestParam(value = "meetingDate", required = false) 
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime meetingDate,
            @RequestParam(value = "participants", required = false) String participantsJson) {

        try {
            if (file.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "文件不能为空");
                return ResponseEntity.badRequest().body(response);
            }

            // 创建会议记录
            MeetingRecord meetingRecord = new MeetingRecord();
            meetingRecord.setMeetingName(meetingName);
            meetingRecord.setMeetingTopic(meetingTopic);
            meetingRecord.setLocation(location);
            meetingRecord.setLanguage(language);
            meetingRecord.setMeetingDate(meetingDate);
            meetingRecord.setOriginalFileName(file.getOriginalFilename());
            meetingRecord.setSourceType(determineSourceType(file.getOriginalFilename()));
            meetingRecord.setProcessingStatus(MeetingRecord.ProcessingStatus.PENDING);
            meetingRecord.setCreatedAt(LocalDateTime.now());
            meetingRecord.setUpdatedAt(LocalDateTime.now());

            // 保存文件
            String filePath = fileProcessingService.getFileStorageService().storeFile(file);
            meetingRecord.setFilePath(filePath);

            // 保存会议记录
            MeetingRecord savedRecord = meetingRecordRepository.save(meetingRecord);

            // 解析并保存参会人员信息
            if (StringUtils.hasText(participantsJson)) {
                try {
                    List<Participant> participants = objectMapper.readValue(participantsJson, 
                            objectMapper.getTypeFactory().constructCollectionType(List.class, Participant.class));
                    
                    for (Participant participant : participants) {
                        if (StringUtils.hasText(participant.getName())) {
                            MeetingParticipant meetingParticipant = new MeetingParticipant();
                            meetingParticipant.setName(participant.getName());
                            meetingParticipant.setRole(participant.getRole());
                            meetingParticipant.setDepartment(participant.getDepartment());
                            meetingParticipant.setEmail(participant.getEmail());
                            meetingParticipant.setPhone(participant.getPhone());
                            meetingParticipant.setAttendanceStatus(participant.getAttendanceStatus());
                            meetingParticipant.setMeetingRecord(savedRecord);
                            participantRepository.save(meetingParticipant);
                        }
                    }
                } catch (JsonProcessingException e) {
                    log.error("解析参会人员信息失败", e);
                }
            }

            // 异步处理文件
            fileProcessingService.processFileAsync(savedRecord.getId());

            // 返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("id", savedRecord.getId());
            result.put("status", "UPLOADED");
            result.put("message", "文件上传成功，开始处理");
            result.put("meetingName", savedRecord.getMeetingName());
            result.put("processingStatus", savedRecord.getProcessingStatus().name());
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("文件上传失败", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "上传失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getMeetingRecords(
            @RequestParam(value = "meetingName", required = false) String meetingName,
            @RequestParam(value = "sourceType", required = false) MeetingRecord.SourceType sourceType,
            @RequestParam(value = "status", required = false) MeetingRecord.ProcessingStatus status,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {

        Specification<MeetingRecord> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(meetingName)) {
                predicates.add(criteriaBuilder.like(root.get("meetingName"), "%" + meetingName + "%"));
            }

            if (sourceType != null) {
                predicates.add(criteriaBuilder.equal(root.get("sourceType"), sourceType));
            }

            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("processingStatus"), status));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, 
                org.springframework.data.domain.Sort.Direction.DESC, "createdAt");
        Page<MeetingRecord> meetingRecordPage = meetingRecordRepository.findAll(spec, pageable);

        // 转换为前端需要的格式
        List<Map<String, Object>> records = meetingRecordPage.getContent().stream().map(record -> {
            Map<String, Object> recordMap = new HashMap<>();
            recordMap.put("id", record.getId());
            recordMap.put("meetingName", record.getMeetingName());
            recordMap.put("meetingTopic", record.getMeetingTopic());
            recordMap.put("sourceType", record.getSourceType().name());
            recordMap.put("processingStatus", record.getProcessingStatus().name());
            recordMap.put("createdAt", record.getCreatedAt().toString());
            recordMap.put("meetingDate", record.getMeetingDate() != null ? record.getMeetingDate().toString() : null);
            recordMap.put("location", record.getLocation());
            recordMap.put("originalFileName", record.getOriginalFileName());
            
            // 计算处理进度（简单示例）
            switch (record.getProcessingStatus()) {
                case PENDING:
                    recordMap.put("progress", 0);
                    break;
                case PROCESSING:
                    recordMap.put("progress", 50);
                    break;
                case COMPLETED:
                    recordMap.put("progress", 100);
                    break;
                case FAILED:
                    recordMap.put("progress", -1);
                    break;
            }
            
            return recordMap;
        }).collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("content", records);
        response.put("totalElements", meetingRecordPage.getTotalElements());
        response.put("totalPages", meetingRecordPage.getTotalPages());
        response.put("currentPage", page);
        response.put("pageSize", size);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getMeetingRecordDetails(@PathVariable Long id) {
        Optional<MeetingRecord> recordOptional = meetingRecordRepository.findById(id);
        
        if (!recordOptional.isPresent()) {
                return ResponseEntity.notFound().build();
            }
        
        MeetingRecord record = recordOptional.get();
        
        // 获取相关实体
        List<MeetingParticipant> participants = participantRepository.findByMeetingRecordId(id);
        List<MeetingTopic> topics = topicRepository.findByMeetingRecordIdOrderByTopicOrderAsc(id);
        List<MeetingDecision> decisions = decisionRepository.findByMeetingRecordIdOrderByDecisionDateDesc(id);
        List<MeetingActionItem> actionItems = actionItemRepository.findByMeetingRecordIdOrderByDueDateAsc(id);
        List<MeetingFollowUp> followUps = followUpRepository.findByMeetingRecordIdOrderByPriorityDesc(id);
        
        // 构建响应
        Map<String, Object> response = new HashMap<>();
        response.put("id", record.getId());
        response.put("meetingName", record.getMeetingName());
        response.put("meetingTopic", record.getMeetingTopic());
        response.put("sourceType", record.getSourceType().name());
        response.put("processingStatus", record.getProcessingStatus().name());
        response.put("createdAt", record.getCreatedAt().toString());
        response.put("meetingDate", record.getMeetingDate() != null ? record.getMeetingDate().toString() : null);
        response.put("location", record.getLocation());
        response.put("language", record.getLanguage());
        response.put("originalFileName", record.getOriginalFileName());
        response.put("filePath", record.getFilePath());
        response.put("transcriptionText", record.getTranscriptionText());
        response.put("analysisResult", record.getAnalysisResult());
        
        // 添加参会人员信息
        response.put("participants", participants.stream().map(p -> {
            Map<String, Object> pMap = new HashMap<>();
            pMap.put("id", p.getId());
            pMap.put("name", p.getName());
            pMap.put("role", p.getRole());
            pMap.put("department", p.getDepartment());
            pMap.put("email", p.getEmail());
            pMap.put("phone", p.getPhone());
            pMap.put("attendanceStatus", p.getAttendanceStatus());
            return pMap;
        }).collect(Collectors.toList()));
        
        // 添加议题信息
        response.put("topics", topics.stream().map(t -> {
            Map<String, Object> tMap = new HashMap<>();
            tMap.put("id", t.getId());
            tMap.put("topicTitle", t.getTopicTitle());
            tMap.put("topicDescription", t.getTopicDescription());
            tMap.put("discussionPoints", t.getDiscussionPoints());
            tMap.put("speakerViews", t.getSpeakerViews());
            tMap.put("topicOrder", t.getTopicOrder());
            return tMap;
        }).collect(Collectors.toList()));
        
        // 添加决策信息
        response.put("decisions", decisions.stream().map(d -> {
            Map<String, Object> dMap = new HashMap<>();
            dMap.put("id", d.getId());
            dMap.put("decisionTitle", d.getDecisionTitle());
            dMap.put("decisionContent", d.getDecisionContent());
            dMap.put("decisionType", d.getDecisionType());
            dMap.put("consensusLevel", d.getConsensusLevel());
            dMap.put("decisionMakers", d.getDecisionMakers());
            dMap.put("decisionDate", d.getDecisionDate() != null ? d.getDecisionDate().toString() : null);
            dMap.put("effectiveDate", d.getEffectiveDate() != null ? d.getEffectiveDate().toString() : null);
            return dMap;
        }).collect(Collectors.toList()));
        
        // 添加行动项信息
        response.put("actionItems", actionItems.stream().map(a -> {
            Map<String, Object> aMap = new HashMap<>();
            aMap.put("id", a.getId());
            aMap.put("taskDescription", a.getTaskDescription());
            aMap.put("responsiblePerson", a.getResponsiblePerson());
            aMap.put("department", a.getDepartment());
            aMap.put("priority", a.getPriority());
            aMap.put("dueDate", a.getDueDate() != null ? a.getDueDate().toString() : null);
            aMap.put("status", a.getStatus());
            aMap.put("progressNotes", a.getProgressNotes());
            return aMap;
        }).collect(Collectors.toList()));
        
        // 添加跟进问题信息
        response.put("followUps", followUps.stream().map(f -> {
            Map<String, Object> fMap = new HashMap<>();
            fMap.put("id", f.getId());
            fMap.put("issueTitle", f.getIssueTitle());
            fMap.put("issueDescription", f.getIssueDescription());
            fMap.put("unresolvedReason", f.getUnresolvedReason());
            fMap.put("followUpPlan", f.getFollowUpPlan());
            fMap.put("nextMeetingDate", f.getNextMeetingDate() != null ? f.getNextMeetingDate().toString() : null);
            fMap.put("responsiblePerson", f.getResponsiblePerson());
            fMap.put("department", f.getDepartment());
            fMap.put("priority", f.getPriority());
            fMap.put("status", f.getStatus());
            fMap.put("targetResolutionDate", f.getTargetResolutionDate() != null ? f.getTargetResolutionDate().toString() : null);
            return fMap;
        }).collect(Collectors.toList()));
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/participants")
    public ResponseEntity<Map<String, Object>> getMeetingRecordParticipants(@PathVariable Long id) {
        Optional<MeetingRecord> recordOptional = meetingRecordRepository.findById(id);
        
        if (!recordOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        List<MeetingParticipant> participants = participantRepository.findByMeetingRecordId(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("meetingId", id);
        response.put("participants", participants.stream().map(p -> {
            Map<String, Object> pMap = new HashMap<>();
            pMap.put("id", p.getId());
            pMap.put("name", p.getName());
            pMap.put("role", p.getRole());
            pMap.put("department", p.getDepartment());
            pMap.put("email", p.getEmail());
            pMap.put("phone", p.getPhone());
            pMap.put("attendanceStatus", p.getAttendanceStatus());
            return pMap;
        }).collect(Collectors.toList()));
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/topics")
    public ResponseEntity<Map<String, Object>> getMeetingRecordTopics(@PathVariable Long id) {
        Optional<MeetingRecord> recordOptional = meetingRecordRepository.findById(id);
        
        if (!recordOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        List<MeetingTopic> topics = topicRepository.findByMeetingRecordIdOrderByTopicOrderAsc(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("meetingId", id);
        response.put("topics", topics.stream().map(t -> {
            Map<String, Object> tMap = new HashMap<>();
            tMap.put("id", t.getId());
            tMap.put("topicTitle", t.getTopicTitle());
            tMap.put("topicDescription", t.getTopicDescription());
            tMap.put("discussionPoints", t.getDiscussionPoints());
            tMap.put("speakerViews", t.getSpeakerViews());
            tMap.put("topicOrder", t.getTopicOrder());
            return tMap;
        }).collect(Collectors.toList()));
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/decisions")
    public ResponseEntity<Map<String, Object>> getMeetingRecordDecisions(@PathVariable Long id) {
        Optional<MeetingRecord> recordOptional = meetingRecordRepository.findById(id);
        
        if (!recordOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        List<MeetingDecision> decisions = decisionRepository.findByMeetingRecordId(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("meetingId", id);
        response.put("decisions", decisions.stream().map(d -> {
            Map<String, Object> dMap = new HashMap<>();
            dMap.put("id", d.getId());
            dMap.put("decisionTitle", d.getDecisionTitle());
            dMap.put("decisionDescription", d.getDecisionDescription());
            dMap.put("decisionMaker", d.getDecisionMaker());
            dMap.put("implementationPlan", d.getImplementationPlan());
            dMap.put("deadline", d.getDeadline());
            dMap.put("priority", d.getPriority());
            dMap.put("status", d.getStatus());
            return dMap;
        }).collect(Collectors.toList()));
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/action-items")
    public ResponseEntity<Map<String, Object>> getMeetingRecordActionItems(@PathVariable Long id) {
        Optional<MeetingRecord> recordOptional = meetingRecordRepository.findById(id);
        
        if (!recordOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        List<MeetingActionItem> actionItems = actionItemRepository.findByMeetingRecordId(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("meetingId", id);
        response.put("actionItems", actionItems.stream().map(a -> {
            Map<String, Object> aMap = new HashMap<>();
            aMap.put("id", a.getId());
            aMap.put("taskDescription", a.getTaskDescription());
            aMap.put("assignedTo", a.getAssignedTo());
            aMap.put("dueDate", a.getDueDate());
            aMap.put("priority", a.getPriority());
            aMap.put("status", a.getStatus());
            aMap.put("completionNotes", a.getCompletionNotes());
            return aMap;
        }).collect(Collectors.toList()));
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/follow-ups")
    public ResponseEntity<Map<String, Object>> getMeetingRecordFollowUps(@PathVariable Long id) {
        Optional<MeetingRecord> recordOptional = meetingRecordRepository.findById(id);
        
        if (!recordOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        List<MeetingFollowUp> followUps = followUpRepository.findByMeetingRecordId(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("meetingId", id);
        response.put("followUps", followUps.stream().map(f -> {
            Map<String, Object> fMap = new HashMap<>();
            fMap.put("id", f.getId());
            fMap.put("issueTitle", f.getIssueTitle());
            fMap.put("issueDescription", f.getIssueDescription());
            fMap.put("unresolvedReason", f.getUnresolvedReason());
            fMap.put("followUpPlan", f.getFollowUpPlan());
            fMap.put("nextMeetingDate", f.getNextMeetingDate());
            fMap.put("responsiblePerson", f.getResponsiblePerson());
            fMap.put("department", f.getDepartment());
            fMap.put("priority", f.getPriority());
            fMap.put("status", f.getStatus());
            fMap.put("targetResolutionDate", f.getTargetResolutionDate());
            return fMap;
        }).collect(Collectors.toList()));
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/reprocess")
    public ResponseEntity<Map<String, Object>> reprocessMeetingRecord(@PathVariable Long id) {
        Optional<MeetingRecord> recordOptional = meetingRecordRepository.findById(id);
        
        if (!recordOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        MeetingRecord record = recordOptional.get();
        record.setProcessingStatus(MeetingRecord.ProcessingStatus.PENDING);
        record.setUpdatedAt(LocalDateTime.now());
        meetingRecordRepository.save(record);
        
        // 异步处理文件
        fileProcessingService.processFileAsync(record.getId());
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "已重新开始处理文件");
        return ResponseEntity.ok(response);
    }

    @RequestMapping(method = RequestMethod.OPTIONS)
    public ResponseEntity<?> handleOptions() {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadFile(@PathVariable Long id) {
        Optional<MeetingRecord> recordOptional = meetingRecordRepository.findById(id);
        
        if (!recordOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        MeetingRecord record = recordOptional.get();
        
        try {
            byte[] fileBytes = org.apache.commons.io.FileUtils.readFileToByteArray(
                    new java.io.File(record.getFilePath()));
            
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"" + record.getOriginalFileName() + "\"")
                    .contentType(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM)
                    .body(fileBytes);
        } catch (java.io.IOException e) {
            log.error("文件下载失败: {}", record.getFilePath(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}/download-summary")
    public ResponseEntity<byte[]> downloadStructuredSummary(@PathVariable Long id) {
        Optional<MeetingRecord> recordOptional = meetingRecordRepository.findById(id);
        
        if (!recordOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        MeetingRecord record = recordOptional.get();
        
        try {
            String summaryContent = generateSummaryContent(record);
            
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"会议纪要_" + record.getMeetingName() + ".txt\"")
                    .contentType(org.springframework.http.MediaType.TEXT_PLAIN)
                    .body(summaryContent.getBytes("UTF-8"));
        } catch (Exception e) {
            log.error("生成会议纪要失败: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    private String generateSummaryContent(MeetingRecord record) {
        StringBuilder sb = new StringBuilder();
        sb.append("会议名称: ").append(record.getMeetingName()).append("\n");
        sb.append("会议主题: ").append(record.getMeetingTopic()).append("\n");
        sb.append("会议时间: ").append(record.getMeetingDate() != null ? record.getMeetingDate().toString() : "未知").append("\n");
        sb.append("会议地点: ").append(record.getLocation() != null ? record.getLocation() : "未知").append("\n");
        sb.append("文件来源: ").append(record.getSourceType() == MeetingRecord.SourceType.AUDIO ? "语音文件" : "文档文件").append("\n");
        sb.append("\n=== 参会人员 ===\n");
        
        // 添加参会人员信息
        List<MeetingParticipant> participants = participantRepository.findByMeetingRecordId(record.getId());
        for (MeetingParticipant participant : participants) {
            sb.append("- ").append(participant.getName())
              .append(" (").append(participant.getRole() != null ? participant.getRole() : "无职务").append(")")
              .append(" - ").append(participant.getDepartment() != null ? participant.getDepartment() : "无部门").append("\n");
        }
        
        sb.append("\n=== 议题讨论 ===\n");
        
        // 添加议题信息
        List<MeetingTopic> topics = topicRepository.findByMeetingRecordIdOrderByTopicOrderAsc(record.getId());
        for (int i = 0; i < topics.size(); i++) {
            MeetingTopic topic = topics.get(i);
            sb.append(i + 1).append(". ").append(topic.getTopicTitle()).append("\n");
            if (topic.getTopicDescription() != null) {
                sb.append("   议题描述: ").append(topic.getTopicDescription()).append("\n");
            }
            if (topic.getDiscussionPoints() != null) {
                sb.append("   讨论要点: ").append(topic.getDiscussionPoints()).append("\n");
            }
            sb.append("\n");
        }
        
        sb.append("\n=== 决策事项 ===\n");
        
        // 添加决策信息
        List<MeetingDecision> decisions = decisionRepository.findByMeetingRecordIdOrderByDecisionDateDesc(record.getId());
        for (int i = 0; i < decisions.size(); i++) {
            MeetingDecision decision = decisions.get(i);
            sb.append(i + 1).append(". ").append(decision.getDecisionTitle()).append("\n");
            sb.append("   决策内容: ").append(decision.getDecisionContent()).append("\n");
            if (decision.getDecisionMakers() != null) {
                sb.append("   决策制定者: ").append(decision.getDecisionMakers()).append("\n");
            }
            sb.append("\n");
        }
        
        sb.append("\n=== 行动项 ===\n");
        
        // 添加行动项信息
        List<MeetingActionItem> actionItems = actionItemRepository.findByMeetingRecordIdOrderByDueDateAsc(record.getId());
        for (int i = 0; i < actionItems.size(); i++) {
            MeetingActionItem actionItem = actionItems.get(i);
            sb.append(i + 1).append(". ").append(actionItem.getTaskDescription()).append("\n");
            sb.append("   责任人: ").append(actionItem.getResponsiblePerson()).append("\n");
            sb.append("   负责部门: ").append(actionItem.getDepartment() != null ? actionItem.getDepartment() : "未指定").append("\n");
            sb.append("   优先级: ").append(actionItem.getPriority() != null ? actionItem.getPriority() : "未指定").append("\n");
            if (actionItem.getDueDate() != null) {
                sb.append("   截止时间: ").append(actionItem.getDueDate().toString()).append("\n");
            }
            sb.append("   状态: ").append(actionItem.getStatus() != null ? actionItem.getStatus() : "未指定").append("\n");
            sb.append("\n");
        }
        
        sb.append("\n=== 待跟进问题 ===\n");
        
        // 添加跟进问题信息
        List<MeetingFollowUp> followUps = followUpRepository.findByMeetingRecordIdOrderByPriorityDesc(record.getId());
        for (int i = 0; i < followUps.size(); i++) {
            MeetingFollowUp followUp = followUps.get(i);
            sb.append(i + 1).append(". ").append(followUp.getIssueTitle()).append("\n");
            sb.append("   问题描述: ").append(followUp.getIssueDescription()).append("\n");
            if (followUp.getFollowUpPlan() != null) {
                sb.append("   跟进计划: ").append(followUp.getFollowUpPlan()).append("\n");
            }
            if (followUp.getResponsiblePerson() != null) {
                sb.append("   负责人: ").append(followUp.getResponsiblePerson()).append("\n");
            }
            sb.append("   优先级: ").append(followUp.getPriority() != null ? followUp.getPriority() : "未指定").append("\n");
            if (followUp.getTargetResolutionDate() != null) {
                sb.append("   目标解决时间: ").append(followUp.getTargetResolutionDate().toString()).append("\n");
            }
            sb.append("   状态: ").append(followUp.getStatus() != null ? followUp.getStatus() : "未指定").append("\n");
            sb.append("\n");
        }
        
        return sb.toString();
    }

    private MeetingRecord.SourceType determineSourceType(String fileName) {
        if (fileName == null) {
            return MeetingRecord.SourceType.DOCUMENT;
        }
        
        String lowerFileName = fileName.toLowerCase();
        if (lowerFileName.endsWith(".mp3") || lowerFileName.endsWith(".wav") || 
            lowerFileName.endsWith(".m4a") || lowerFileName.endsWith(".flac")) {
            return MeetingRecord.SourceType.AUDIO;
        }
        
        return MeetingRecord.SourceType.DOCUMENT;
    }

    // 内部类用于解析JSON
    public static class Participant {
        private String name;
        private String role;
        private String department;
        private String email;
        private String phone;
        private String attendanceStatus;

        // getters and setters
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getDepartment() {
            return department;
        }

        public void setDepartment(String department) {
            this.department = department;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getAttendanceStatus() {
            return attendanceStatus;
        }

        public void setAttendanceStatus(String attendanceStatus) {
            this.attendanceStatus = attendanceStatus;
        }
    }
}