package com.elite.employeemanager.project.service;

import com.elite.employeemanager.auth.user.entity.User;
import com.elite.employeemanager.project.entity.Project;
import com.elite.employeemanager.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;

    private User getCurrentUser(){
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(principal instanceof User) {
            return  ((User) principal);
        }
        return null;
    }

    @Transactional
    public Project addProject(Project project){

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
        return projectRepository.save(project);
    }

    public List<Project> getAllProjects(){
        return projectRepository.findAll();
    }

    public Project getProjectById(Long id){
        return projectRepository.findById(id)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Project Not Found with Id : "+id));
    }

    @Transactional
    public Project updateProjectById(Long id, Project updatedProject){
        Project existingProject = getProjectById(id);

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
        existingProject.setIsDeleted(true);
        existingProject.setDeletedAt(LocalDateTime.now());
        if (getCurrentUser()!=null){
            existingProject.setDeletedBy(getCurrentUser().getId());
        }
        existingProject.setDeleteReason(reason);
        if (!"COMPLETED".equalsIgnoreCase(existingProject.getStatus())){
            existingProject.setStatus("CANCELLED");
        }
        projectRepository.save(existingProject);
    }

}
