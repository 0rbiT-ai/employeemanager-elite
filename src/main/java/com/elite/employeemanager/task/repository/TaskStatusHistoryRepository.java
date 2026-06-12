package com.elite.employeemanager.task.repository;

import com.elite.employeemanager.task.entity.Task;
import com.elite.employeemanager.task.entity.TaskStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskStatusHistoryRepository extends JpaRepository<TaskStatusHistory,Long> {
    List<TaskStatusHistory> findByTask(Task task);
}
