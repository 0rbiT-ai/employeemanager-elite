package com.elite.employeemanager.employee.service;

import com.elite.employeemanager.auth.mapping.entity.UserRole;
import com.elite.employeemanager.auth.mapping.repository.UserRoleRepository;
import com.elite.employeemanager.auth.role.entity.Role;
import com.elite.employeemanager.auth.role.repository.RoleRepository;
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
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;

    private void populateRoles(Employee employee){
        if (employee==null||employee.getUser()==null) return;

        List<UserRole> userRoles = userRoleRepository.findByUser(employee.getUser());
        List<String> roles = userRoles.stream().map(ur->ur.getRole().getRoleCode()).toList();

        employee.setRoles(roles);
    }

    private User getCurrentUser(){
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(principal instanceof User) {
            return  ((User) principal);
        }
        return null;
    }

    public Employee addEmployee(Employee employee){
        if (employee.getUser()==null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"User Credentials are required to add employee");
        }
        User userPayload = employee.getUser();
        User savedUser;

        Optional<User> existingUserOpt = userRepository.findByEmail(employee.getWorkEmail());
        if (existingUserOpt.isPresent()){

            User existingUser = existingUserOpt.get();
            existingUser.setIsActive(!"INACTIVE".equalsIgnoreCase(employee.getStatus()));
            existingUser.setPasswordHash(passwordEncoder.encode(userPayload.getRawPassword()));

            existingUser.setPasswordLastUpdatedAt(LocalDateTime.now());
            savedUser = userRepository.save(existingUser);
        }
        else{
            User newUser = User.builder()
                    .email(employee.getWorkEmail())
                    .passwordHash(passwordEncoder.encode(userPayload.getRawPassword()))
                    .passwordLastUpdatedAt(LocalDateTime.now())
                    .isActive(!"INACTIVE".equalsIgnoreCase(employee.getStatus()))
                    .build();
            savedUser = userRepository.save(newUser);
        }

        employee.setUser(savedUser);
        userRoleRepository.deleteAll(userRoleRepository.findByUser(savedUser));

        List<String> roles = employee.getRoles();
        if (roles==null||roles.isEmpty()){
            roles=List.of("Employee");
        }

        for (String roleStr:roles){
            String roleCode = roleStr.toUpperCase().trim().replace(" ","_");
            if ("ADMINISTRATOR".equals(roleCode)) roleCode="ADMIN";

            Role role = roleRepository.findByRoleCode(roleCode)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role not found: " + roleStr));

            UserRole userRoleMapping = UserRole.builder()
                    .user(savedUser)
                    .role(role)
                    .assignedAt(LocalDateTime.now())
                    .assignedBy(getCurrentUser())
                    .build();
            userRoleRepository.save(userRoleMapping);
        }

        return employeeRepository.save(employee);
    }

    public List<Employee> getAllEmployees(){
        List<Employee> employees = employeeRepository.findAll();
        employees.forEach(this::populateRoles);
        return employees;
    }

    public Employee getEmployeeById(Long id){
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Employee Not Found"));
        populateRoles(employee);
        return employee;
    }

    public Employee updateEmployeeById(Long id, Employee updateEmployee) {
        Employee employee = getEmployeeById(id);

        if (updateEmployee.getEmployeeCode()!=null){
            employee.setEmployeeCode(updateEmployee.getEmployeeCode());
        }
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
                    existingUserPayload.setIsActive(false);
                } else if ("ACTIVE".equalsIgnoreCase(updateEmployee.getStatus())||"ON_LEAVE".equalsIgnoreCase(updateEmployee.getStatus())){
                    existingUserPayload.setIsActive(true);
                }
                employee.setStatus(updateEmployee.getStatus());
            }

            if (updateEmployee.getWorkEmail()!=null && !updateEmployee.getWorkEmail().isEmpty()){
                employee.setWorkEmail(updateEmployee.getWorkEmail());
                existingUserPayload.setEmail(updateEmployee.getWorkEmail());
            }

            if (updateEmployee.getUser() != null && updateEmployee.getUser().getRawPassword() != null && !updateEmployee.getUser().getRawPassword().isEmpty()) {
                User updatedUserPayload = updateEmployee.getUser();
                String newRawPassword = updatedUserPayload.getRawPassword();
                String newHashedPassword = passwordEncoder.encode(newRawPassword);
                existingUserPayload.setPasswordLastUpdatedAt(LocalDateTime.now());
                existingUserPayload.setPasswordHash(newHashedPassword);
            }

            if (updateEmployee.getRoles()!=null){
                List<UserRole> existingRoles = userRoleRepository.findByUser(existingUserPayload);
                userRoleRepository.deleteAll(existingRoles);

                for (String roleStr: updateEmployee.getRoles()){
                    String roleCode = roleStr.trim().replace(" ","_");
                    if ("ADMINISTRATOR".equals(roleCode)) roleCode="ADMIN";

                    Role role = roleRepository.findByRoleCode(roleCode)
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role not found: " + roleStr));

                    UserRole userRoleMapping = UserRole.builder()
                            .user(existingUserPayload)
                            .role(role)
                            .assignedAt(LocalDateTime.now())
                            .assignedBy(getCurrentUser())
                            .build();
                    userRoleRepository.save(userRoleMapping);
                }
            }

            userRepository.save(existingUserPayload);
        }
        return employeeRepository.save(employee);
    }

    public void deleteEmployeeById(Long id, String reason){
        Employee employee = getEmployeeById(id);

        employee.setIsDeleted(true);
        employee.setDeletedAt(LocalDateTime.now());
        employee.setDeletedBy(getCurrentUser().getId());
        employee.setDeleteReason(reason);
        employee.setStatus("INACTIVE");

        if (employee.getUser() != null) {
            User user = employee.getUser();
            user.setIsActive(false);
            userRepository.save(user);
        }

        employeeRepository.save(employee);
    }

}
