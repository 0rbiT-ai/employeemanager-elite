package com.elite.employeemanager.auth.jwt.utils;

import com.elite.employeemanager.auth.user.entity.User;
import com.elite.employeemanager.employee.entity.Employee;
import com.elite.employeemanager.employee.repository.EmployeeRepository;
import com.elite.employeemanager.auth.mapping.entity.UserRole;
import com.elite.employeemanager.auth.mapping.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class SecurityUtils {

    private final EmployeeRepository employeeRepository;
    private final UserRoleRepository userRoleRepository;

    public Employee getCurrentEmployee() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof User user) {
            Employee employee = employeeRepository.findByWorkEmail(user.getEmail())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Employee record not found"));
            
            List<UserRole> userRoles = userRoleRepository.findByUser(user);
            List<String> roles = userRoles.stream()
                    .map(ur -> ur.getRole().getRoleCode())
                    .toList();
            employee.setRoles(roles);

            return employee;
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof User user) {
            return user;
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
    }

}
