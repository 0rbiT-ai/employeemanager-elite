package com.elite.employeemanager.project.service;

import com.elite.employeemanager.employee.entity.Employee;
import com.elite.employeemanager.employee.repository.EmployeeRepository;
import com.elite.employeemanager.project.entity.Project;
import com.elite.employeemanager.project.entity.ProjectEmployee;
import com.elite.employeemanager.project.repository.ProjectEmployeeRepository;
import com.elite.employeemanager.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectEmployeeService {

    private final ProjectEmployeeRepository projectEmployeeRepository;
    private final EmployeeRepository employeeRepository;
    private final ProjectRepository projectRepository;

    @Transactional
    public ProjectEmployee addEmployeeToProject(Long projectId, Long employeeId){
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Employee Not Found"));
        Project project = projectRepository.findById(projectId)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Project Not Found"));

        if (projectEmployeeRepository.findByProjectAndEmployee(project,employee).isPresent()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Employee is already member of this project");
        }
        ProjectEmployee projectEmployee = ProjectEmployee.builder()
                .employee(employee)
                .project(project)
                .build();

        return projectEmployeeRepository.save(projectEmployee);
    }

    @Transactional
    public void removeEmployeeFromProject(Long projectId, Long employeeId){
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Employee Not Found"));
        Project project = projectRepository.findById(projectId)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Project Not Found"));
        ProjectEmployee projectEmployee = projectEmployeeRepository.findByProjectAndEmployee(project,employee)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Employee does not belong to this project"));
        projectEmployeeRepository.delete(projectEmployee);
    }

    public List<Employee> getMembersByProjectId(Long projectId){
        Project project = projectRepository.findById(projectId)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Project Not Found"));
        List<ProjectEmployee> projectEmployees = projectEmployeeRepository.findByProject(project);
        return projectEmployees.stream().map(ProjectEmployee::getEmployee).toList();
    }

    public List<Project> getProjectsByEmployeeId(Long employeeId){
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Employee Not Found"));
        List<ProjectEmployee> projectEmployees = projectEmployeeRepository.findByEmployee(employee);
        return projectEmployees.stream().map(ProjectEmployee::getProject).toList();
    }

}
