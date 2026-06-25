package com.elite.employeemanager.meeting.repository;

import com.elite.employeemanager.meeting.entity.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting,Long> {
    @Query("SELECT DISTINCT m FROM Meeting m " +
            "LEFT JOIN m.attendees a " +
            "WHERE a.id = :employeeId OR m.createdBy = :userId " +
            "ORDER BY m.startTime Asc")
    List<Meeting> findPersonalMeetings(@Param("employeeId") Long employeeId, @Param("userId") Long userId);
}
