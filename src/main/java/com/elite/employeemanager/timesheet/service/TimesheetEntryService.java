package com.elite.employeemanager.timesheet.service;

import com.elite.employeemanager.employee.entity.Employee;
import com.elite.employeemanager.employee.repository.EmployeeRepository;
import com.elite.employeemanager.project.entity.Project;
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
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TimesheetEntryService {

    private final TimesheetEntryRepository timesheetEntryRepository;
    private final EmployeeRepository employeeRepository;
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;

    private TimesheetResponse mapToResponse(TimesheetEntry entry) {
        return TimesheetResponse.builder()
                .id(entry.getId())
                .employee(new TimesheetResponse.NestedId(entry.getEmployee().getId()))
                .task(new TimesheetResponse.NestedId(entry.getTask().getId()))
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
                .build();
    }

    public List<TimesheetResponse> getAllEntries(Long employeeId, LocalDate date, String status){

        Specification<TimesheetEntry> spec = Specification.unrestricted();
        if (employeeId!=null){
            spec = spec.and((root,query,cb)->
                    cb.equal(root.get("employee").get("id"),employeeId)
            );
        }
        if (date!=null){
            spec = spec.and((root,query,cb)->
                    cb.equal(root.get("workDate"),date)
            );
        }
        if (status!=null){
            spec = spec.and((root,query,cb)->
                    cb.equal(root.get("status"),status)
            );
        }

        return timesheetEntryRepository.findAll(spec)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public TimesheetResponse createEntry(TimesheetRequest request){
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Employee not found"));

        Task task = taskRepository.findById(request.getTaskId())
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND,"Task not found"));

        Project project = null;
        if (request.getProjectId()!=null){
            project = projectRepository.findById(request.getProjectId())
                    .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND,"Project not found"));
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
                .hoursSpent(request.getDurationHours())
                .description(request.getDescription())
                .justification(request.getJustification())
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .build();

        return mapToResponse(timesheetEntryRepository.save(entry));
    }

    @Transactional
    public TimesheetResponse updateStatus(Long id, TimesheetStatusUpdateRequest request){
        TimesheetEntry entry = timesheetEntryRepository.findById(id)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Entry not found"));
        entry.setStatus(request.getStatus().toUpperCase());
        entry.setManagerComment(request.getManagerComment());

        return mapToResponse(timesheetEntryRepository.save(entry));
    }

    @Transactional
    public void deleteEntry(Long id) {
        if (!timesheetEntryRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Time entry not found");
        }
        timesheetEntryRepository.deleteById(id);
    }
}
