package com.elite.employeemanager.project.repository;

import com.elite.employeemanager.project.entity.ProjectTeam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectTeamRepository extends JpaRepository<ProjectTeam,Long> {
}
