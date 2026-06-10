package com.elite.employeemanager.project.repository;

import com.elite.employeemanager.project.entity.ProjectMilestone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectMilestoneRepository extends JpaRepository<ProjectMilestone,Long> {
}
