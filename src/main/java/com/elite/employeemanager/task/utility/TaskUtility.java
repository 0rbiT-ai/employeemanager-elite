package com.elite.employeemanager.task.utility;

import com.elite.employeemanager.auth.jwt.utils.SecurityUtils;
import com.elite.employeemanager.employee.entity.Employee;
import com.elite.employeemanager.project.repository.ProjectEmployeeRepository;
import com.elite.employeemanager.task.entity.Task;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
@RequiredArgsConstructor
public class TaskUtility {

    private final SecurityUtils securityUtils;
    private final ProjectEmployeeRepository projectEmployeeRepository;

    public void validateProjectMembership(Task task) {
        Employee currentEmployee = securityUtils.getCurrentEmployee();
        if (currentEmployee.getRoles().contains("ADMIN")) return;

        boolean isMember = projectEmployeeRepository.findByProjectAndEmployee(task.getProject(), currentEmployee).isPresent();
        if (!isMember) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Current User is not a member of this task's project");
        }
    }

    public void validateProjectManagementAccess(Task task) {
        Employee currentEmployee = securityUtils.getCurrentEmployee();
        if (currentEmployee.getRoles().contains("ADMIN")) return;

        if (!currentEmployee.getRoles().contains("TEAM_LEAD") && !currentEmployee.getRoles().contains("SUB_LEAD")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Current User is not allowed to manage tasks");
        }

        boolean isMember = projectEmployeeRepository.findByProjectAndEmployee(task.getProject(), currentEmployee).isPresent();
        if (!isMember) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Current User is not a member of this task's project");
        }
    }
}
