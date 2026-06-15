package com.elite.employeemanager.task.repository;

import com.elite.employeemanager.task.entity.TaskAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskAttachmentRepository extends JpaRepository<TaskAttachment,Long> {
}
