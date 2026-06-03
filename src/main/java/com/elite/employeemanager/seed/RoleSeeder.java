package com.elite.employeemanager.seed;

import com.elite.employeemanager.role.entity.Role;
import com.elite.employeemanager.role.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class RoleSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    @Override
    public void run(String... args) throws Exception {
        seedRole(
                "ADMIN",
                "Admin",
                "Read-everything watchdog. Creates employees, teams, projects. Gets alert flags for anomalies."
        );

        seedRole(
                "TEAM_LEAD",
                "Team Lead",
                "Full operational control. Creates and assigns tasks. Approves timesheets and ETA extensions."
        );

        seedRole(
                "SUB_LEAD",
                "Sub Lead",
                "Same as TEAM_LEAD with limited override permissions. Stored as a role tag."
        );

        seedRole(
                "EMPLOYEE",
                "Employee",
                "Clocks in/out, logs hours, updates task progress, requests ETA extensions and reassignments."
        );
    }

    private void seedRole(String roleCode, String roleName, String description) {
        if(roleRepository.findByRoleCode(roleCode).isEmpty()){
            Role role = Role.builder()
                            .roleCode(roleCode)
                            .name(roleName)
                            .description(description)
                            .build();

            roleRepository.save(role);
        }
    }
}
