package com.meeting.manager.repository;

import com.meeting.manager.entity.MeetingRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MeetingRecordRepository extends JpaRepository<MeetingRecord, Long>, JpaSpecificationExecutor<MeetingRecord> {
    
    @Query("SELECT m FROM MeetingRecord m WHERE " +
           "(:meetingName IS NULL OR m.meetingName LIKE %:meetingName%) AND " +
           "(:sourceType IS NULL OR m.sourceType = :sourceType) AND " +
           "(:startDate IS NULL OR m.meetingDate >= :startDate) AND " +
           "(:endDate IS NULL OR m.meetingDate <= :endDate) AND " +
           "(:status IS NULL OR m.processingStatus = :status)")
    Page<MeetingRecord> findByConditions(
            @Param("meetingName") String meetingName,
            @Param("sourceType") MeetingRecord.SourceType sourceType,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("status") MeetingRecord.ProcessingStatus status,
            Pageable pageable);
    
    List<MeetingRecord> findByProcessingStatus(MeetingRecord.ProcessingStatus status);
    
    @Query("SELECT m FROM MeetingRecord m WHERE m.processingStatus = 'PROCESSING' ORDER BY m.createdAt DESC")
    List<MeetingRecord> findProcessingRecords();
    
    @Query("SELECT m FROM MeetingRecord m WHERE m.createdAt >= :startDate ORDER BY m.createdAt DESC")
    List<MeetingRecord> findRecentRecords(@Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT COUNT(m) FROM MeetingRecord m WHERE m.createdAt >= :startDate")
    Long countRecentRecords(@Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT COUNT(m) FROM MeetingRecord m WHERE m.createdAt BETWEEN :startDate AND :endDate")
    Long countByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(m) FROM MeetingRecord m WHERE m.processingStatus = :status AND m.createdAt BETWEEN :startDate AND :endDate")
    Long countByProcessingStatusAndCreatedAtBetween(
            @Param("status") MeetingRecord.ProcessingStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(m) FROM MeetingRecord m WHERE m.sourceType = :sourceType AND m.createdAt BETWEEN :startDate AND :endDate")
    Long countBySourceTypeAndCreatedAtBetween(
            @Param("sourceType") MeetingRecord.SourceType sourceType,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}