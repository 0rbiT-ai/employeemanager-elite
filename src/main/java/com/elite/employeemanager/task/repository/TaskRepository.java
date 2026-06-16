package com.elite.employeemanager.task.repository;

import com.elite.employeemanager.employee.entity.Employee;
import com.elite.employeemanager.project.entity.Project;
import com.elite.employeemanager.task.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task,Long> {
    boolean existsByTaskNumber(String taskNumber);

    List<Task> findByAssignedTo(Employee assignedTo);

    List<Task> findByProject(Project project);

    List<Task> findByAssignedToIsNull();

    List<Task> findByProjectAndAssignedTo(Project project, Employee employee);
}
