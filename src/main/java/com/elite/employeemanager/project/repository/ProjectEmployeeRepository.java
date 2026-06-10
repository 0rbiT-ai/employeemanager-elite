package com.elite.employeemanager.project.repository;

import com.elite.employeemanager.employee.entity.Employee;
import com.elite.employeemanager.project.entity.Project;
import com.elite.employeemanager.project.entity.ProjectEmployee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectEmployeeRepository extends JpaRepository<ProjectEmployee,Long> {
    Optional<ProjectEmployee> findByProjectAndEmployee(Project project, Employee employee);

    List<ProjectEmployee> findByProject(Project project);

    List<ProjectEmployee> findByEmployee(Employee employee);
}
