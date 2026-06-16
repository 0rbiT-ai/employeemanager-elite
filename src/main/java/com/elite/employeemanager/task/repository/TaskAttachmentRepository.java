package com.elite.employeemanager.task.repository;

import com.elite.employeemanager.task.entity.Task;
import com.elite.employeemanager.task.entity.TaskAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskAttachmentRepository extends JpaRepository<TaskAttachment,Long> {
    Optional<TaskAttachment> findByIdAndTaskId(Long attachmentId,Long taskId);

    List<TaskAttachment> findByTask(Task task);
}
