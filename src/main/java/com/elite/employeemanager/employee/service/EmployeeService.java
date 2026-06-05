package com.elite.employeemanager.employee.service;

import com.elite.employeemanager.auth.user.entity.User;
import com.elite.employeemanager.employee.entity.Employee;
import com.elite.employeemanager.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository employeeRepository;

    public Employee addEmployee(Employee employee){
        return employeeRepository.save(employee);
    }

    public List<Employee> getAllEmployees(){
        return employeeRepository.findAll();
    }

    public Employee getEmployeeById(Long id){
        return employeeRepository.findById(id)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Employee Not Found"));
    }

    public Employee updateEmployeeById(Long id, Employee updateEmployee){
        Employee employee = getEmployeeById(id);

        employee.setName(updateEmployee.getName());
        employee.setPersonalEmail(updateEmployee.getPersonalEmail());
        employee.setPhone(updateEmployee.getPhone());
        employee.setDesignation(updateEmployee.getDesignation());
        employee.setJoiningDate(updateEmployee.getJoiningDate());
        employee.setStatus(updateEmployee.getStatus());
        employee.setNotificationPreference(updateEmployee.getNotificationPreference());
        employee.setProfileImage(updateEmployee.getProfileImage());

        return employeeRepository.save(employee);
    }

    public void deleteEmployeeById(Long id, String reason){
        Employee employee = getEmployeeById(id);

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long currentUserId = null;
        if(principal != null) {
            currentUserId = ((User) principal).getId();
        }

        employee.setDeleted(true);
        employee.setDeletedAt(LocalDateTime.now());
        employee.setDeletedBy(currentUserId);
        employee.setDeleteReason(reason);
        employeeRepository.save(employee);
    }

}
