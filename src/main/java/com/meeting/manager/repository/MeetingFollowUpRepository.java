package com.meeting.manager.repository;

import com.meeting.manager.entity.MeetingFollowUp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MeetingFollowUpRepository extends JpaRepository<MeetingFollowUp, Long> {
    
    List<MeetingFollowUp> findByMeetingRecordId(Long meetingRecordId);
    
    List<MeetingFollowUp> findByMeetingRecordIdOrderByPriorityDesc(Long meetingRecordId);
    
    List<MeetingFollowUp> findByStatus(String status);
    
    List<MeetingFollowUp> findByResponsiblePerson(String responsiblePerson);
    
    @Query("SELECT f FROM MeetingFollowUp f WHERE f.status = 'OPEN' AND f.targetResolutionDate <= :dueDate ORDER BY f.targetResolutionDate ASC")
    List<MeetingFollowUp> findOverdueFollowUps(@Param("dueDate") LocalDateTime dueDate);
}