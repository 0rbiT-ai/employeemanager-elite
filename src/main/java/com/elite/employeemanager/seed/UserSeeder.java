package com.elite.employeemanager.seed;

import com.elite.employeemanager.auth.mapping.entity.UserRole;
import com.elite.employeemanager.auth.mapping.repository.UserRoleRepository;
import com.elite.employeemanager.auth.role.entity.Role;
import com.elite.employeemanager.auth.role.repository.RoleRepository;
import com.elite.employeemanager.auth.user.entity.User;
import com.elite.employeemanager.auth.user.repository.UserRepository;
import com.elite.employeemanager.employee.entity.Employee;
import com.elite.employeemanager.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Order(2)
public class UserSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRoleRepository userRoleRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    public void run(String... args) throws Exception {
        seedUser("admin@teamops.com", "admin123", "ADMIN");
        seedUser("employee@teamops.com", "employee123", "EMPLOYEE");
    }

    private void seedUser(String email, String rawPassword, String roleCode) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            // 1. Create User
            user = User.builder()
                    .email(email)
                    .passwordHash(passwordEncoder.encode(rawPassword))
                    .isActive(true)
                    .passwordLastUpdatedAt(LocalDateTime.now())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            user = userRepository.save(user);

            // 2. Fetch target Role
            Role role = roleRepository.findByRoleCode(roleCode)
                    .orElseThrow(() -> new IllegalStateException(roleCode + " role not seeded yet!"));

            // 3. Map User to Role
            UserRole mapping = UserRole.builder()
                    .user(user)
                    .role(role)
                    .assignedAt(LocalDateTime.now())
                    .build();
            userRoleRepository.save(mapping);
            System.out.println("-------------------------------------------");
            System.out.println("Seeded Created Successfully: " + email);
            System.out.println("Password: " + rawPassword);
            System.out.println("Role: " + roleCode);
            System.out.println("-------------------------------------------");
        }

        // 4. Create matching Employee if missing
        if (employeeRepository.findByWorkEmail(email).isEmpty()) {
            String code = "ADMIN".equalsIgnoreCase(roleCode) ? "EMP-001" : "EMP-002";
            String name = "ADMIN".equalsIgnoreCase(roleCode) ? "Admin User" : "Employee User";
            String designation = "ADMIN".equalsIgnoreCase(roleCode) ? "Administrator" : "Software Engineer";
            String phone = "ADMIN".equalsIgnoreCase(roleCode) ? "0000000000" : "1111111111";

            Employee employee = Employee.builder()
                    .employeeCode(code)
                    .name(name)
                    .workEmail(email)
                    .phone(phone)
                    .designation(designation)
                    .user(user)
                    .joiningDate(LocalDate.now())
                    .status("ACTIVE")
                    .notificationPreference("ALL")
                    .build();
            employeeRepository.save(employee);
            System.out.println("Employee record seeded for: " + email);
        }
    }
}
