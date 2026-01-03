package com.meeting.manager.repository;

import com.meeting.manager.entity.MeetingDecision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MeetingDecisionRepository extends JpaRepository<MeetingDecision, Long> {
    
    List<MeetingDecision> findByMeetingRecordId(Long meetingRecordId);
    
    List<MeetingDecision> findByMeetingRecordIdOrderByDecisionDateDesc(Long meetingRecordId);
}