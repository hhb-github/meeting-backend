package com.meeting.manager.repository;

import com.meeting.manager.entity.MeetingParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MeetingParticipantRepository extends JpaRepository<MeetingParticipant, Long> {
    
    List<MeetingParticipant> findByMeetingRecordId(Long meetingRecordId);
    
    List<MeetingParticipant> findByMeetingRecordIdAndAttendanceStatus(Long meetingRecordId, String attendanceStatus);
}