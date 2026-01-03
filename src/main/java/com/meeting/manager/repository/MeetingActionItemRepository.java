package com.meeting.manager.repository;

import com.meeting.manager.entity.MeetingActionItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MeetingActionItemRepository extends JpaRepository<MeetingActionItem, Long> {
    
    List<MeetingActionItem> findByMeetingRecordId(Long meetingRecordId);
    
    List<MeetingActionItem> findByMeetingRecordIdOrderByDueDateAsc(Long meetingRecordId);
    
    List<MeetingActionItem> findByResponsiblePerson(String responsiblePerson);
    
    List<MeetingActionItem> findByStatus(String status);
    
    @Query("SELECT a FROM MeetingActionItem a WHERE a.status = 'PENDING' AND a.dueDate <= :dueDate ORDER BY a.dueDate ASC")
    List<MeetingActionItem> findOverdueItems(@Param("dueDate") LocalDateTime dueDate);
    
    @Query("SELECT a FROM MeetingActionItem a WHERE a.status = 'PENDING' AND a.dueDate BETWEEN :startDate AND :endDate ORDER BY a.dueDate ASC")
    List<MeetingActionItem> findUpcomingItems(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}