package com.elite.employeemanager.task.repository;

import com.elite.employeemanager.employee.entity.Employee;
import com.elite.employeemanager.project.entity.Project;
import com.elite.employeemanager.task.entity.EtaExtension;
import com.elite.employeemanager.task.entity.Task;
import com.elite.employeemanager.task.entity.TaskTransfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskTransferRepository extends JpaRepository<TaskTransfer,Long> {
    boolean existsByTaskAndStatus(Task task, String status);

    List<TaskTransfer> findByTask(Task task);

    void deleteByTaskProjectAndTargetEmployeeAndStatus(Project project,Employee targetEmployee, String status);

    void deleteByTaskProjectAndRequestedByAndStatus(Project project, Employee employee, String pending);

    void deleteByTargetEmployeeAndStatus(Employee employee, String pending);

    void deleteByRequestedByAndStatus(Employee employee, String pending);
}
