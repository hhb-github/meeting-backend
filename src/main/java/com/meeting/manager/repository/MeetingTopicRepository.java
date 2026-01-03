package com.meeting.manager.repository;

import com.meeting.manager.entity.MeetingTopic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MeetingTopicRepository extends JpaRepository<MeetingTopic, Long> {
    
    List<MeetingTopic> findByMeetingRecordIdOrderByTopicOrderAsc(Long meetingRecordId);
    
    @Query("SELECT t FROM MeetingTopic t WHERE t.meetingRecord.id = :meetingRecordId ORDER BY t.topicOrder ASC")
    List<MeetingTopic> findByMeetingRecordIdOrderByOrder(Long meetingRecordId);
}