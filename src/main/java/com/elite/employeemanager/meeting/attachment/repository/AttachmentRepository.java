package com.elite.employeemanager.meeting.attachment.repository;

import com.elite.employeemanager.meeting.attachment.entity.Attachment;
import com.elite.employeemanager.meeting.entity.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment,Long> {
    List<Attachment> findByMeeting(Meeting meeting);
}
