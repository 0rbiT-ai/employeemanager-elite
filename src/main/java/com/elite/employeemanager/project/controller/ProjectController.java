package com.elite.employeemanager.project.controller;

import com.elite.employeemanager.project.entity.Project;
import com.elite.employeemanager.project.service.ProjectService;
import com.elite.employeemanager.task.entity.Task;
import com.elite.employeemanager.task.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final TaskService taskService;

    @PostMapping
    @PreAuthorize("hasAuthority('PROJECT_CREATE')")
    public ResponseEntity<Project> addProject(@RequestBody Project project){
        return new ResponseEntity<>(projectService.addProject(project), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PROJECT_VIEW')")
    public ResponseEntity<List<Project>> getAllProjects(){
        return new ResponseEntity<>(projectService.getAllProjects(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PROJECT_VIEW')")
    public ResponseEntity<Project> getProjectById(@PathVariable Long id){
        return new ResponseEntity<>(projectService.getProjectById(id), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PROJECT_UPDATE')")
    public ResponseEntity<Project> updateProjectById(@PathVariable Long id, @RequestBody Project project){
        return new ResponseEntity<>(projectService.updateProjectById(id,project),HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PROJECT_DELETE')")
    public ResponseEntity<String> deleteProjectById(@PathVariable Long id, @RequestBody String reason){
        projectService.deleteProjectById(id,reason);
        return new ResponseEntity<>("Project Deleted", HttpStatus.OK);
    }

    @GetMapping("/{id}/tasks")
    @PreAuthorize("hasAuthority('PROJECT_VIEW')")
    private ResponseEntity<List<Task>> getTasksByProjectId(@PathVariable Long id){
        return new ResponseEntity<>(taskService.getTasksByProjectId(id),HttpStatus.OK);
    }
}
