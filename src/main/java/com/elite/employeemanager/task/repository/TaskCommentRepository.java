package com.elite.employeemanager.task.repository;

import com.elite.employeemanager.employee.entity.Employee;
import com.elite.employeemanager.task.entity.Task;
import com.elite.employeemanager.task.entity.TaskComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskCommentRepository extends JpaRepository<TaskComment,Long> {
    List<TaskComment> findByTask(Task task);

    void deleteByTask(Task task);
}
