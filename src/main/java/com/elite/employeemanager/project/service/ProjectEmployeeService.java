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
import com.elite.employeemanager.team.entity.Team;
import com.elite.employeemanager.team.entity.TeamEmployee;
import com.elite.employeemanager.team.repository.TeamRepository;
import com.elite.employeemanager.team.repository.TeamEmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ProjectEmployeeService {

    private final ProjectEmployeeRepository projectEmployeeRepository;
    private final EmployeeRepository employeeRepository;
    private final ProjectRepository projectRepository;
    private final TaskTransferRepository taskTransferRepository;
    private final TaskRepository taskRepository;
    private final TaskService taskService;
    private final TeamRepository teamRepository;
    private final TeamEmployeeRepository teamEmployeeRepository;
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

        boolean teamMemberInProject = false;
        if (currentEmployee.getRoles().contains("TEAM_LEAD") || currentEmployee.getRoles().contains("SUB_LEAD")) {
            List<Team> leadTeams = teamRepository.findByLead(currentEmployee);
            List<Team> subLeadTeams = teamRepository.findBySubLead(currentEmployee);

            List<Employee> teamMembers = Stream.of(
                    leadTeams.isEmpty() ? List.<TeamEmployee>of() : teamEmployeeRepository.findByTeamIn(leadTeams),
                    subLeadTeams.isEmpty() ? List.<TeamEmployee>of() : teamEmployeeRepository.findByTeamIn(subLeadTeams)
            )
            .flatMap(List::stream)
            .map(TeamEmployee::getEmployee)
            .distinct()
            .toList();

            if (!teamMembers.isEmpty()) {
                teamMemberInProject = projectEmployeeRepository.findByEmployeeIn(teamMembers).stream()
                        .anyMatch(pe -> pe.getProject().getId().equals(project.getId()));
            }
        }

        // Allow Team Leads and Sub Leads to add/remove themselves to/from any project, or if they or their team member is in the project
        if (!isMember && !teamMemberInProject && !currentEmployee.getId().equals(targetEmployeeId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Current User is not member of this project and has no team members in it"
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
                boolean hasAccessAsLead = false;
                if (currentEmployee.getRoles().contains("TEAM_LEAD") || currentEmployee.getRoles().contains("SUB_LEAD")) {
                    List<Team> leadTeams = teamRepository.findByLead(currentEmployee);
                    List<Team> subLeadTeams = teamRepository.findBySubLead(currentEmployee);

                    List<Employee> teamMembers = Stream.of(
                            leadTeams.isEmpty() ? List.<TeamEmployee>of() : teamEmployeeRepository.findByTeamIn(leadTeams),
                            subLeadTeams.isEmpty() ? List.<TeamEmployee>of() : teamEmployeeRepository.findByTeamIn(subLeadTeams)
                    )
                    .flatMap(List::stream)
                    .map(TeamEmployee::getEmployee)
                    .distinct()
                    .toList();

                    if (!teamMembers.isEmpty()) {
                        hasAccessAsLead = projectEmployeeRepository.findByEmployeeIn(teamMembers).stream()
                                .anyMatch(pe -> pe.getProject().getId().equals(projectId));
                    }
                }
                if (!hasAccessAsLead) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Current User Not Member of Project");
                }
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

            boolean isManagedEmployee = false;
            if (currentEmployee.getRoles().contains("TEAM_LEAD") || currentEmployee.getRoles().contains("SUB_LEAD")) {
                List<Team> leadTeams = teamRepository.findByLead(currentEmployee);
                List<Team> subLeadTeams = teamRepository.findBySubLead(currentEmployee);

                isManagedEmployee = Stream.of(
                        leadTeams.isEmpty() ? List.<TeamEmployee>of() : teamEmployeeRepository.findByTeamIn(leadTeams),
                        subLeadTeams.isEmpty() ? List.<TeamEmployee>of() : teamEmployeeRepository.findByTeamIn(subLeadTeams)
                )
                .flatMap(List::stream)
                .map(TeamEmployee::getEmployee)
                .anyMatch(e -> e.getId().equals(employeeId));
            }

            if (!isManagedEmployee) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Current User is not allowed to view another employee's projects"
                );
            }
        }

        List<ProjectEmployee> projectEmployees = projectEmployeeRepository.findByEmployee(employee);
        return projectEmployees.stream().map(ProjectEmployee::getProject).toList();
    }

}
