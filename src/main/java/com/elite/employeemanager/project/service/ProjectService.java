package com.elite.employeemanager.project.service;

import com.elite.employeemanager.employee.entity.Employee;
import com.elite.employeemanager.project.entity.Project;
import com.elite.employeemanager.project.entity.ProjectEmployee;
import com.elite.employeemanager.project.repository.ProjectEmployeeRepository;
import com.elite.employeemanager.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.elite.employeemanager.auth.jwt.utils.SecurityUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectEmployeeRepository projectEmployeeRepository;
    private final SecurityUtils securityUtils;

    @Transactional
    public Project addProject(Project project){

        Employee employee = securityUtils.getCurrentEmployee();
        if (!employee.getRoles().contains("ADMIN") && !employee.getRoles().contains("TEAM_LEAD") && !employee.getRoles().contains("SUB_LEAD")){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Current User is not allowed to create projects");
        }

        if (project.getColorHex() == null) {
            project.setColorHex("#8ECAE6");
        }
        if (project.getStatus() == null) {
            project.setStatus("ACTIVE");
        }
        if (project.getProgressPercentage() == null) {
            project.setProgressPercentage(0);
        }

        if (project.getProjectName()==null || project.getProjectName().isBlank()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Project Name is required");
        }

        if (project.getClientName()==null || project.getClientName().isBlank()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Client Name is required");
        }

        if (project.getStartDate()==null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Project Start Date is required");
        }

        if (project.getEndDate()!=null && project.getEndDate().isBefore(project.getStartDate())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End date cannot be before start date");
        }
        
        Project savedProject = projectRepository.save(project);

        if (employee.getRoles().contains("TEAM_LEAD") || employee.getRoles().contains("SUB_LEAD")) {
            ProjectEmployee projectEmployee = ProjectEmployee.builder()
                    .project(savedProject)
                    .employee(employee)
                    .build();
            projectEmployeeRepository.save(projectEmployee);
        }

        return savedProject;
    }

    public List<Project> getAllProjects(){
        Employee employee = securityUtils.getCurrentEmployee();
        if (!employee.getRoles().contains("ADMIN")){
            List<ProjectEmployee> memberships = projectEmployeeRepository.findByEmployee(employee);
            return memberships.stream().map(ProjectEmployee::getProject).toList();
        }

        return projectRepository.findAll();
    }

    public Project getProjectById(Long id){
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project Not Found with Id : " + id));

        Employee employee = securityUtils.getCurrentEmployee();

        if (!employee.getRoles().contains("ADMIN")){
            boolean isMember = projectEmployeeRepository.findByProjectAndEmployee(project, employee).isPresent();
            if (!isMember){
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Current User Not Member of Project");
            }
        }
        return project;
    }

    @Transactional
    public Project updateProjectById(Long id, Project updatedProject){
        Project existingProject = getProjectById(id);

        Employee employee = securityUtils.getCurrentEmployee();
        if (!employee.getRoles().contains("ADMIN") && !employee.getRoles().contains("TEAM_LEAD") && !employee.getRoles().contains("SUB_LEAD")){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Current User is not allowed to update project");
        }

        if (updatedProject.getProjectName()!=null){
            if (updatedProject.getProjectName().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Project Name cannot be blank");
            }
            existingProject.setProjectName(updatedProject.getProjectName());
        }

        if (updatedProject.getDescription()!=null){
            existingProject.setDescription(updatedProject.getDescription());
        }

        if (updatedProject.getClientName()!=null){
            if (updatedProject.getClientName().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Client Name cannot be blank");
            }
            existingProject.setClientName(updatedProject.getClientName());
        }

        if (updatedProject.getColorHex()!=null){
            existingProject.setColorHex(updatedProject.getColorHex());
        }

        LocalDate finalStartDate = updatedProject.getStartDate()!=null?updatedProject.getStartDate():existingProject.getStartDate();
        LocalDate finalEndDate = updatedProject.getEndDate()!=null?updatedProject.getEndDate():existingProject.getEndDate();
        if (finalEndDate!=null && finalEndDate.isBefore(finalStartDate)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End date cannot be before start date");
        }
        existingProject.setStartDate(finalStartDate);
        existingProject.setEndDate(finalEndDate);

        if (updatedProject.getStatus()!=null){
            existingProject.setStatus(updatedProject.getStatus());
        }

        if (updatedProject.getProgressPercentage()!=null){
            existingProject.setProgressPercentage(updatedProject.getProgressPercentage());
        }

        return projectRepository.save(existingProject);
    }

    @Transactional
    public void deleteProjectById(Long id, String reason){
        Project existingProject = getProjectById(id);

        Employee employee = securityUtils.getCurrentEmployee();
        if (!employee.getRoles().contains("ADMIN") && !employee.getRoles().contains("TEAM_LEAD") && !employee.getRoles().contains("SUB_LEAD")){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Current User is not allowed to delete project");
        }

        projectEmployeeRepository.deleteByProject(existingProject);

        existingProject.setIsDeleted(true);
        existingProject.setDeletedAt(LocalDateTime.now());
        existingProject.setDeletedBy(securityUtils.getCurrentUser().getId());
        existingProject.setDeleteReason(reason);
        if (!"COMPLETED".equalsIgnoreCase(existingProject.getStatus())){
            existingProject.setStatus("CANCELLED");
        }
        projectRepository.save(existingProject);
    }

}
