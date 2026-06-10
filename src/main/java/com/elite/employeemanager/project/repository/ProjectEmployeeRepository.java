package com.elite.employeemanager.project.repository;

import com.elite.employeemanager.project.entity.ProjectEmployee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectEmployeeRepository extends JpaRepository<ProjectEmployee,Long> {
}
