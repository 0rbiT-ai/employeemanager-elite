package com.elite.employeemanager.task.repository;

import com.elite.employeemanager.task.entity.TaskDraftBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskDraftBatchRepository extends JpaRepository<TaskDraftBatch,Long> {

    Optional<TaskDraftBatch> findByCreatedByAndStatus(Long userId, String open);

    List<TaskDraftBatch> findAllByStatusAndCreatedAtAfter(String status, LocalDateTime since);
}
