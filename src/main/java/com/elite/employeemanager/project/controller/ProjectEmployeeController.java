package com.elite.employeemanager.project.controller;

import com.elite.employeemanager.employee.entity.Employee;
import com.elite.employeemanager.project.entity.ProjectEmployee;
import com.elite.employeemanager.project.service.ProjectEmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectEmployeeController {

    private final ProjectEmployeeService projectEmployeeService;

    @PostMapping("/{projectId}/employees/{employeeId}")
    @PreAuthorize("hasAuthority('PROJECT_UPDATE')")
    public ResponseEntity<ProjectEmployee> addEmployeeToProject(@PathVariable Long projectId, @PathVariable Long employeeId){
        return new ResponseEntity<>(projectEmployeeService.addEmployeeToProject(projectId,employeeId), HttpStatus.CREATED);
    }

    @DeleteMapping("/{projectId}/employees/{employeeId}")
    @PreAuthorize("hasAuthority('PROJECT_UPDATE')")
    public ResponseEntity<String> removeEmployeeFromProject(@PathVariable Long projectId, @PathVariable Long employeeId){
        projectEmployeeService.removeEmployeeFromProject(projectId,employeeId);
        return new ResponseEntity<>("Employee Removed from Project", HttpStatus.OK);
    }

    @GetMapping("/{projectId}/employees")
    @PreAuthorize("hasAuthority('PROJECT_VIEW')")
    public ResponseEntity<List<Employee>> getMembersByProjectId(@PathVariable Long projectId){
        return new ResponseEntity<>(projectEmployeeService.getMembersByProjectId(projectId), HttpStatus.OK);
    }
}
