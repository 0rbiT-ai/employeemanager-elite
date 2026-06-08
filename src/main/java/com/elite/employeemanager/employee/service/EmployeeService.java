package com.elite.employeemanager.employee.service;

import com.elite.employeemanager.auth.user.entity.User;
import com.elite.employeemanager.auth.user.repository.UserRepository;
import com.elite.employeemanager.employee.entity.Employee;
import com.elite.employeemanager.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public Employee addEmployee(Employee employee){
        if (employee.getUser()==null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"User Credentials are required to add employee");
        }
        User userPayload = employee.getUser();
        User newUser = User.builder()
                .email(employee.getWorkEmail())
                .passwordHash(passwordEncoder.encode(userPayload.getPassword()))
                .passwordLastUpdatedAt(LocalDateTime.now())
                .isActive(!"INACTIVE".equalsIgnoreCase(employee.getStatus()))
                .build();
        User savedUser = userRepository.save(newUser);
        employee.setUser(savedUser);
        return employeeRepository.save(employee);
    }

    public List<Employee> getAllEmployees(){
        return employeeRepository.findAll();
    }

    public Employee getEmployeeById(Long id){
        return employeeRepository.findById(id)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Employee Not Found"));
    }

    public Employee updateEmployeeById(Long id, Employee updateEmployee) {
        Employee employee = getEmployeeById(id);

        if (updateEmployee.getName()!=null){
            employee.setName(updateEmployee.getName());
        }
        if (updateEmployee.getPersonalEmail()!=null){
            employee.setPersonalEmail(updateEmployee.getPersonalEmail());
        }
        if (updateEmployee.getPhone()!=null){
            employee.setPhone(updateEmployee.getPhone());
        }
        if (updateEmployee.getDesignation()!=null){
            employee.setDesignation(updateEmployee.getDesignation());
        }
        if (updateEmployee.getJoiningDate()!=null){
            employee.setJoiningDate(updateEmployee.getJoiningDate());
        }
        if (updateEmployee.getNotificationPreference()!=null){
            employee.setNotificationPreference(updateEmployee.getNotificationPreference());
        }
        if (updateEmployee.getProfileImage()!=null){
            employee.setProfileImage(updateEmployee.getProfileImage());
        }
        if (updateEmployee.getStatus()!=null){
            employee.setStatus(updateEmployee.getStatus());
        }

        if (employee.getUser() != null) {
            User existingUserPayload = employee.getUser();

            if (updateEmployee.getStatus()!=null){
                if ("INACTIVE".equalsIgnoreCase(updateEmployee.getStatus())) {
                    existingUserPayload.setActive(false);
                } else if ("ACTIVE".equalsIgnoreCase(updateEmployee.getStatus())||"ON_LEAVE".equalsIgnoreCase(updateEmployee.getStatus())){
                    existingUserPayload.setActive(true);
                }
                employee.setStatus(updateEmployee.getStatus());
            }

            if (updateEmployee.getWorkEmail()!=null && !updateEmployee.getWorkEmail().isEmpty()){
                employee.setWorkEmail(updateEmployee.getWorkEmail());
                existingUserPayload.setEmail(updateEmployee.getWorkEmail());
            }

            if (updateEmployee.getUser() != null && updateEmployee.getUser().getPassword() != null && !updateEmployee.getUser().getPassword().isEmpty()) {
                User updatedUserPayload = updateEmployee.getUser();
                String newRawPassword = updatedUserPayload.getPassword();
                String newHashedPassword = passwordEncoder.encode(newRawPassword);
                existingUserPayload.setPasswordLastUpdatedAt(LocalDateTime.now());
                existingUserPayload.setPasswordHash(newHashedPassword);
            }

            userRepository.save(existingUserPayload);
        }
        return employeeRepository.save(employee);
    }

    public void deleteEmployeeById(Long id, String reason){
        Employee employee = getEmployeeById(id);

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long currentUserId = null;
        if(principal instanceof User) {
            currentUserId = ((User) principal).getId();
        }

        employee.setDeleted(true);
        employee.setDeletedAt(LocalDateTime.now());
        employee.setDeletedBy(currentUserId);
        employee.setDeleteReason(reason);
        employee.setStatus("INACTIVE");

        if (employee.getUser() != null) {
            User user = employee.getUser();
            user.setActive(false); 
            userRepository.save(user);
        }

        employeeRepository.save(employee);
    }

}
