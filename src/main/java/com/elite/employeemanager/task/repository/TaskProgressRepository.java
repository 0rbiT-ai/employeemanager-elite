package com.elite.employeemanager.task.repository;

import com.elite.employeemanager.task.entity.Task;
import com.elite.employeemanager.task.entity.TaskProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskProgressRepository extends JpaRepository<TaskProgress,Long> {
    List<TaskProgress> findByTask(Task task);
}
