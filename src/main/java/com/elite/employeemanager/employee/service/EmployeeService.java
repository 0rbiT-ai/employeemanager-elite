package com.elite.employeemanager.employee.service;

import com.elite.employeemanager.auth.user.entity.User;
import com.elite.employeemanager.auth.user.repository.UserRepository;
import com.elite.employeemanager.employee.entity.Employee;
import com.elite.employeemanager.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.parameters.P;
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
                .isActive(true)
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

        if(employee.getUser()!=null && updateEmployee.getUser()!=null){
            User existingUserPayload = employee.getUser();
            User updatedUserPayload = updateEmployee.getUser();

            if (updatedUserPayload.getEmail()!=null && !updatedUserPayload.getEmail().isEmpty()){
                String newEmail = updatedUserPayload.getEmail();
                employee.setWorkEmail(newEmail);
                existingUserPayload.setEmail(newEmail);
            }
            if (updatedUserPayload.getPassword()!=null && !updatedUserPayload.getPassword().isEmpty()){
                String newRawPassword = updatedUserPayload.getPassword();
                String newHashedPassword = passwordEncoder.encode(newRawPassword);
                existingUserPayload.setPasswordLastUpdatedAt(LocalDateTime.now());
                existingUserPayload.setPasswordHash(newHashedPassword);
            }
            userRepository.save(existingUserPayload);
        }
        if(employee.getUser()!=null){
            User existingUserPayload = employee.getUser();
            if (updateEmployee.getStatus()!=null){
                if("INACTIVE".equalsIgnoreCase(updateEmployee.getStatus())){
                    existingUserPayload.setActive(false);
                }
                else {
                    existingUserPayload.setActive(true);
                }
            }
            userRepository.save(existingUserPayload);
        }
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

        if (employee.getUser() != null) {
            User user = employee.getUser();
            user.setActive(false); 
            userRepository.save(user);
        }

        employeeRepository.save(employee);
    }

}
