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
@PreAuthorize("hasAuthority('PROJECT_MANAGE')")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<Project> addProject(@RequestBody Project project){
        return new ResponseEntity<>(projectService.addProject(project), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Project>> getAllProjects(){
        return new ResponseEntity<>(projectService.getAllProjects(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Project> getProjectById(@PathVariable Long id){
        return new ResponseEntity<>(projectService.getProjectById(id), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Project> updateProjectById(@PathVariable Long id, @RequestBody Project project){
        return new ResponseEntity<>(projectService.updateProjectById(id,project),HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProjectById(@PathVariable Long id, @RequestBody String reason){
        projectService.deleteProjectById(id,reason);
        return new ResponseEntity<>("Project Deleted", HttpStatus.OK);
    }

    @GetMapping("/{id}/tasks")
    private ResponseEntity<List<Task>> getTasksByProjectId(@PathVariable Long id){
        return new ResponseEntity<>(taskService.getTasksByProjectId(id),HttpStatus.OK);
    }
}
