package com.elite.employeemanager.seed;

import com.elite.employeemanager.auth.mapping.entity.UserRole;
import com.elite.employeemanager.auth.mapping.repository.UserRoleRepository;
import com.elite.employeemanager.auth.role.entity.Role;
import com.elite.employeemanager.auth.role.repository.RoleRepository;
import com.elite.employeemanager.auth.user.entity.User;
import com.elite.employeemanager.auth.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Order(2)
public class UserSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRoleRepository userRoleRepository;

    @Override
    public void run(String... args) throws Exception {
        seedUser("admin@teamops.com", "admin123", "ADMIN");
        seedUser("employee@teamops.com", "employee123", "EMPLOYEE");
    }

    private void seedUser(String email, String rawPassword, String roleCode) {
        if (userRepository.findByEmail(email).isEmpty()) {
            // 1. Create User
            User user = User.builder()
                    .email(email)
                    .passwordHash(passwordEncoder.encode(rawPassword))
                    .isActive(true)
                    .passwordLastUpdatedAt(LocalDateTime.now())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            User savedUser = userRepository.save(user);

            // 2. Fetch target Role
            Role role = roleRepository.findByRoleCode(roleCode)
                    .orElseThrow(() -> new IllegalStateException(roleCode + " role not seeded yet!"));

            // 3. Map User to Role
            UserRole mapping = UserRole.builder()
                    .user(savedUser)
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
    }
}
