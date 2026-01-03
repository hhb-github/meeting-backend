package com.meeting.manager.controller;

import com.meeting.manager.entity.MeetingRecord;
import com.meeting.manager.repository.MeetingRecordRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/dashboard")
@Slf4j
public class DashboardController {

    @Autowired
    private MeetingRecordRepository meetingRecordRepository;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats(
            @RequestParam(value = "startDate", required = false) 
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(value = "endDate", required = false) 
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        
        try {
            // 如果没有指定日期范围，默认查询最近30天
            if (startDate == null) {
                startDate = LocalDate.now().minusDays(30);
            }
            if (endDate == null) {
                endDate = LocalDate.now();
            }

            // 设置时间范围（包含当天结束时间）
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

            // 查询总文件数
            long totalFiles = meetingRecordRepository.countByCreatedAtBetween(startDateTime, endDateTime);

            // 查询各状态的文件数
            long pendingFiles = meetingRecordRepository.countByProcessingStatusAndCreatedAtBetween(
                    MeetingRecord.ProcessingStatus.PENDING, startDateTime, endDateTime);
            long processingFiles = meetingRecordRepository.countByProcessingStatusAndCreatedAtBetween(
                    MeetingRecord.ProcessingStatus.PROCESSING, startDateTime, endDateTime);
            long completedFiles = meetingRecordRepository.countByProcessingStatusAndCreatedAtBetween(
                    MeetingRecord.ProcessingStatus.COMPLETED, startDateTime, endDateTime);
            long failedFiles = meetingRecordRepository.countByProcessingStatusAndCreatedAtBetween(
                    MeetingRecord.ProcessingStatus.FAILED, startDateTime, endDateTime);

            // 查询今日处理的文件数
            LocalDateTime todayStart = LocalDate.now().atStartOfDay();
            LocalDateTime todayEnd = LocalDate.now().atTime(23, 59, 59);
            long todayProcessed = meetingRecordRepository.countByCreatedAtBetween(todayStart, todayEnd);

            // 查询本周处理的文件数
            LocalDate weekStart = LocalDate.now().minusDays(LocalDate.now().getDayOfWeek().getValue() - 1);
            LocalDateTime weekStartTime = weekStart.atStartOfDay();
            LocalDateTime weekEndTime = LocalDate.now().atTime(23, 59, 59);
            long weekProcessed = meetingRecordRepository.countByCreatedAtBetween(weekStartTime, weekEndTime);

            // 查询音频文件和文档文件的数量
            long audioFiles = meetingRecordRepository.countBySourceTypeAndCreatedAtBetween(
                    MeetingRecord.SourceType.AUDIO, startDateTime, endDateTime);
            long documentFiles = meetingRecordRepository.countBySourceTypeAndCreatedAtBetween(
                    MeetingRecord.SourceType.DOCUMENT, startDateTime, endDateTime);

            // 构造返回数据
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalFiles", totalFiles);
            stats.put("pendingFiles", pendingFiles);
            stats.put("processingFiles", processingFiles);
            stats.put("completedFiles", completedFiles);
            stats.put("failedFiles", failedFiles);
            stats.put("todayProcessed", todayProcessed);
            stats.put("weekProcessed", weekProcessed);
            stats.put("audioFiles", audioFiles);
            stats.put("documentFiles", documentFiles);
            
            // 计算处理成功率
            long successfulFiles = completedFiles;
            long totalProcessedFiles = successfulFiles + failedFiles;
            double successRate = totalProcessedFiles > 0 ? (double) successfulFiles / totalProcessedFiles * 100 : 0;
            stats.put("successRate", Math.round(successRate * 100.0) / 100.0);

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("获取仪表板统计数据失败", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "获取统计数据失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("service", "meeting-manager");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/recent-files")
    public ResponseEntity<Map<String, Object>> getRecentFiles(
            @RequestParam(value = "limit", defaultValue = "5") int limit) {
        
        try {
            // 限制limit参数的最大值
            limit = Math.min(limit, 20);
            
            Pageable pageable = PageRequest.of(0, limit, 
                    org.springframework.data.domain.Sort.Direction.DESC, "createdAt");
            Page<MeetingRecord> page = meetingRecordRepository.findAll(pageable);

            // 转换为前端需要的格式
            List<Map<String, Object>> records = page.getContent().stream().map(record -> {
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
            }).collect(java.util.stream.Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("content", records);
            response.put("totalElements", page.getTotalElements());
            response.put("totalPages", page.getTotalPages());
            response.put("currentPage", page.getNumber());
            response.put("pageSize", page.getSize());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取最近文件列表失败", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "获取文件列表失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}