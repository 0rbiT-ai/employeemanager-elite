package com.elite.employeemanager.timesheet.service;

import com.elite.employeemanager.auth.jwt.utils.SecurityUtils;
import com.elite.employeemanager.employee.entity.Employee;
import com.elite.employeemanager.employee.repository.EmployeeRepository;
import com.elite.employeemanager.project.entity.Project;
import com.elite.employeemanager.project.repository.ProjectEmployeeRepository;
import com.elite.employeemanager.project.repository.ProjectRepository;
import com.elite.employeemanager.task.entity.Task;
import com.elite.employeemanager.task.repository.TaskRepository;
import com.elite.employeemanager.timesheet.dto.*;
import com.elite.employeemanager.timesheet.entity.TimesheetEntry;
import com.elite.employeemanager.timesheet.repository.TimesheetEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import com.elite.employeemanager.team.repository.TeamRepository;
import com.elite.employeemanager.team.repository.TeamEmployeeRepository;
import com.elite.employeemanager.team.entity.Team;

@Service
@RequiredArgsConstructor
public class TimesheetEntryService {

    private final TimesheetEntryRepository timesheetEntryRepository;
    private final EmployeeRepository employeeRepository;
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final ProjectEmployeeRepository projectEmployeeRepository;
    private final SecurityUtils securityUtils;
    private final TeamRepository teamRepository;
    private final TeamEmployeeRepository teamEmployeeRepository;

    private void validateMembership(Project project, Task task, Employee employee) {
        if (project != null) {
            boolean isMember = projectEmployeeRepository.findByProjectAndEmployee(project, employee).isPresent();
            if (!isMember) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Employee is not member of project: " + project.getProjectName());
            }
        }

        if (task != null) {
            if (project != null && task.getProject() != null && !task.getProject().getId().equals(project.getId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The selected task does not belong to the selected project");
            }

            if (task.getAssignedTo() == null || !task.getAssignedTo().getId().equals(employee.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot log entry for a task that is not assigned to you");
            }
        }
    }

    private TimesheetResponse mapToResponse(TimesheetEntry entry) {
        return TimesheetResponse.builder()
                .id(entry.getId())
                .employee(new TimesheetResponse.NestedId(entry.getEmployee().getId()))
                .task(entry.getTask() != null ? new TimesheetResponse.NestedId(entry.getTask().getId()) : null)
                .project(entry.getProject() != null ? new TimesheetResponse.NestedId(entry.getProject().getId()) : null)
                .bugNumber(entry.getBugNumber())
                .workCategory(entry.getWorkCategory())
                .date(entry.getWorkDate())
                .startTime(entry.getStartTime())
                .endTime(entry.getEndTime())
                .durationHours(entry.getHoursSpent())
                .description(entry.getDescription())
                .justification(entry.getJustification())
                .status(entry.getStatus())
                .managerComment(entry.getManagerComment())
                .approvedBy(entry.getApprovedBy() != null ? new TimesheetResponse.NestedId(entry.getApprovedBy().getId()) : null)
                .approvedAt(entry.getApprovedAt())
                .build();
    }

    public List<TimesheetResponse> getAllEntries(Long employeeId, LocalDate date, String status) {
        Specification<TimesheetEntry> spec = Specification.unrestricted();
        if (employeeId != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("employee").get("id"), employeeId)
            );
        }
        if (date != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("workDate"), date)
            );
        }
        if (status != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("status"), status.toUpperCase())
            );
        }

        return timesheetEntryRepository.findAll(spec)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public TimesheetResponse createEntry(TimesheetRequest request) {

        if (request.getEmployee() == null || request.getEmployee().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Employee ID is required");
        }

        if (request.getWorkCategory() == null || request.getWorkCategory().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Work category is required");
        }

        if (request.getStartTime() == null || request.getEndTime() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start time and end time are required");
        }

        if (request.getDate() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Work date is required");
        }

        if (request.getDescription() == null || request.getDescription().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Description is required");
        }

        if (!request.getEndTime().isAfter(request.getStartTime())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Entry end time cannot be before start time");
        }

        BigDecimal calculatedDuration = BigDecimal.valueOf(java.time.Duration.between(request.getStartTime(), request.getEndTime()).toMinutes())
                .divide(BigDecimal.valueOf(60), 2, java.math.RoundingMode.HALF_UP);

        BigDecimal durationHours = request.getDurationHours();
        if (durationHours == null) {
            durationHours = calculatedDuration;
        } else if (durationHours.compareTo(calculatedDuration) != 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Logged duration hours (" + durationHours + ") does not match start and end time difference (" + calculatedDuration + ")");
        }

        Employee currentEmployee = securityUtils.getCurrentEmployee();
        boolean isAdmin = currentEmployee.getRoles().contains("ADMIN");
        if (!isAdmin && !currentEmployee.getId().equals(request.getEmployee().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to log timesheets for other employees");
        }

        Employee employee = employeeRepository.findById(request.getEmployee().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));

        Task task = null;
        Project project = null;

        if (request.getTask() != null && request.getTask().getId() != null) {
            task = taskRepository.findById(request.getTask().getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
            project = task.getProject();
        } else if (request.getProject() != null && request.getProject().getId() != null) {
            project = projectRepository.findById(request.getProject().getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
        }

        validateMembership(project, task, employee);

        boolean isBreak = "BREAK".equalsIgnoreCase(request.getWorkCategory());
        if (!isBreak && task == null && project == null && (request.getBugNumber() == null || request.getBugNumber().isBlank())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Work logs must be linked to at least one reference (task, project, or bug).");
        }


        TimesheetEntry entry = TimesheetEntry.builder()
                .employee(employee)
                .task(task)
                .project(project)
                .bugNumber(request.getBugNumber())
                .workCategory(request.getWorkCategory().toUpperCase())
                .workDate(request.getDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .hoursSpent(durationHours)
                .description(request.getDescription())
                .justification(request.getJustification())
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .build();

        return mapToResponse(timesheetEntryRepository.save(entry));
    }

    @Transactional
    public TimesheetResponse updateStatus(Long id, TimesheetStatusUpdateRequest request) {
        TimesheetEntry entry = timesheetEntryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entry not found"));

        Employee currentEmployee = securityUtils.getCurrentEmployee();
        boolean isAdmin = currentEmployee.getRoles().contains("ADMIN");
        boolean isLead = currentEmployee.getRoles().contains("TEAM_LEAD") ||
                currentEmployee.getRoles().contains("SUB_LEAD");

        if (!isAdmin && !isLead) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Current user cannot approve or reject entry");
        }

        if (entry.getEmployee().getId().equals(currentEmployee.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot approve or reject own entry.");
        }

        if (!isAdmin) {
            List<Team> managedTeams = new java.util.ArrayList<>();
            if (currentEmployee.getRoles().contains("TEAM_LEAD")) {
                managedTeams.addAll(teamRepository.findByLead(currentEmployee));
            }
            if (currentEmployee.getRoles().contains("SUB_LEAD")) {
                managedTeams.addAll(teamRepository.findBySubLead(currentEmployee));
            }

            boolean isMemberOfManagedTeam = false;
            for (Team team : managedTeams) {
                if (teamEmployeeRepository.findByTeamAndEmployee(team, entry.getEmployee()).isPresent()) {
                    isMemberOfManagedTeam = true;
                    break;
                }
            }

            if (!isMemberOfManagedTeam) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to approve or reject timesheet entries for this employee");
            }
        }

        entry.setStatus(request.getStatus().toUpperCase());
        entry.setManagerComment(request.getManagerComment());
        entry.setApprovedBy(currentEmployee);
        entry.setApprovedAt(LocalDateTime.now());

        return mapToResponse(timesheetEntryRepository.save(entry));
    }

    @Transactional
    public void deleteEntry(Long id) {
        Employee currentEmployee = securityUtils.getCurrentEmployee();
        TimesheetEntry entry = timesheetEntryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Timesheet entry not found"));

        boolean isAdmin = currentEmployee.getRoles().contains("ADMIN");
        if (!isAdmin && !entry.getEmployee().getId().equals(currentEmployee.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to delete other employees' time logs");
        }

        timesheetEntryRepository.delete(entry);
    }
}
