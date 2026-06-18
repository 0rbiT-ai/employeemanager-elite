package com.elite.employeemanager.project.service;

import com.elite.employeemanager.auth.jwt.utils.SecurityUtils;
import com.elite.employeemanager.employee.entity.Employee;
import com.elite.employeemanager.employee.repository.EmployeeRepository;
import com.elite.employeemanager.project.entity.Project;
import com.elite.employeemanager.project.entity.ProjectEmployee;
import com.elite.employeemanager.project.repository.ProjectEmployeeRepository;
import com.elite.employeemanager.project.repository.ProjectRepository;
import com.elite.employeemanager.task.entity.Task;
import com.elite.employeemanager.task.repository.TaskRepository;
import com.elite.employeemanager.task.repository.TaskTransferRepository;
import com.elite.employeemanager.task.service.TaskService;
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
    private final TaskTransferRepository taskTransferRepository;
    private final TaskRepository taskRepository;
    private final TaskService taskService;
    private final SecurityUtils securityUtils;

    private void validateProjectManagementAccess(Project project, Long targetEmployeeId) {
        Employee currentEmployee = securityUtils.getCurrentEmployee();

        if (currentEmployee.getRoles().contains("ADMIN")) {
            return;
        }

        if (!currentEmployee.getRoles().contains("TEAM_LEAD")
                && !currentEmployee.getRoles().contains("SUB_LEAD")) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Current User is not allowed to manage project members"
            );
        }

        boolean isMember = projectEmployeeRepository
                .findByProjectAndEmployee(project, currentEmployee)
                .isPresent();

        // Allow Team Leads and Sub Leads to add/remove themselves to/from any project
        if (!isMember && !currentEmployee.getId().equals(targetEmployeeId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Current User is not member of this project"
            );
        }
    }

    @Transactional
    public ProjectEmployee addEmployeeToProject(Long projectId, Long employeeId){
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Employee Not Found"));
        Project project = projectRepository.findById(projectId)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Project Not Found"));

        validateProjectManagementAccess(project, employeeId);

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

        validateProjectManagementAccess(project, employeeId);

        ProjectEmployee projectEmployee = projectEmployeeRepository.findByProjectAndEmployee(project,employee)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Employee does not belong to this project"));

        taskTransferRepository.deleteByTaskProjectAndTargetEmployeeAndStatus(project,employee,"PENDING");
        taskTransferRepository.deleteByTaskProjectAndRequestedByAndStatus(project,employee,"PENDING");

        List<Task> tasks = taskRepository.findByProjectAndAssignedTo(project,employee);
        tasks.forEach(task -> taskService.unassignTaskById(task.getId()));

        projectEmployeeRepository.delete(projectEmployee);
    }

    public List<Employee> getMembersByProjectId(Long projectId){
        Project project = projectRepository.findById(projectId)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Project Not Found"));

        Employee currentEmployee = securityUtils.getCurrentEmployee();
        if (!currentEmployee.getRoles().contains("ADMIN")) {
            boolean isMember = projectEmployeeRepository.findByProjectAndEmployee(project, currentEmployee).isPresent();
            if (!isMember) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Current User Not Member of Project");
            }
        }

        List<ProjectEmployee> projectEmployees = projectEmployeeRepository.findByProject(project);
        return projectEmployees.stream().map(ProjectEmployee::getEmployee).toList();
    }

    public List<Project> getProjectsByEmployeeId(Long employeeId){
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Employee Not Found"));

        Employee currentEmployee = securityUtils.getCurrentEmployee();
        if (!currentEmployee.getRoles().contains("ADMIN")
                && !currentEmployee.getId().equals(employeeId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Current User is not allowed to view another employee's projects"
            );
        }

        List<ProjectEmployee> projectEmployees = projectEmployeeRepository.findByEmployee(employee);
        return projectEmployees.stream().map(ProjectEmployee::getProject).toList();
    }

}
