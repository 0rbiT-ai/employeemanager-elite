package com.elite.employeemanager.seed;

import com.elite.employeemanager.auth.mapping.entity.RolePermission;
import com.elite.employeemanager.auth.mapping.repository.RolePermissionRepository;
import com.elite.employeemanager.auth.permission.entity.Permission;
import com.elite.employeemanager.auth.permission.repository.PermissionRepository;
import com.elite.employeemanager.auth.role.entity.Role;
import com.elite.employeemanager.auth.role.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Order(3)
public class PermissionSeeder implements CommandLineRunner {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final RolePermissionRepository rolePermissionRepository;

    @Override
    public void run(String... args) throws Exception {
        Permission userCreate = seedPermission("USER_CREATE", "Create system user credentials");
        Permission empManage = seedPermission("EMPLOYEE_MANAGE", "Onboard and edit employee profiles");
        Permission taskCreate = seedPermission("TASK_CREATE", "Create task tickets");
        Permission taskAssign = seedPermission("TASK_ASSIGN", "Assign tasks to employees");
        Permission timesheetApprove = seedPermission("TIMESHEET_APPROVE", "Approve and reject timesheets");
        Permission timesheetSubmit = seedPermission("TIMESHEET_SUBMIT", "Submit weekly timesheets");
        Permission teamManage = seedPermission("TEAM_MANAGE", "Create teams and assign/manage members");
        // 2. Fetch Roles
        Role admin = roleRepository.findByRoleCode("ADMIN").orElseThrow();
        Role teamLead = roleRepository.findByRoleCode("TEAM_LEAD").orElseThrow();
        Role employee = roleRepository.findByRoleCode("EMPLOYEE").orElseThrow();
        // 3. Map Permissions to Roles
        // Admin gets all
        mapPermissionToRole(admin, userCreate);
        mapPermissionToRole(admin, empManage);
        mapPermissionToRole(admin, taskCreate);
        mapPermissionToRole(admin, taskAssign);
        mapPermissionToRole(admin, timesheetApprove);
        mapPermissionToRole(admin, timesheetSubmit);
        mapPermissionToRole(admin, teamManage);
        // Team Lead gets task management and approval rights
        mapPermissionToRole(teamLead, taskCreate);
        mapPermissionToRole(teamLead, taskAssign);
        mapPermissionToRole(teamLead, timesheetApprove);
        mapPermissionToRole(teamLead, timesheetSubmit);
        mapPermissionToRole(teamLead,teamManage);
        // Employee can only submit timesheets
        mapPermissionToRole(employee, timesheetSubmit);
    }

    private Permission seedPermission(String name, String description) {
        return permissionRepository.findByPermissionName(name)
                .orElseGet(() -> permissionRepository.save(
                        Permission.builder().permissionName(name).description(description).build()
                ));
    }

    private void mapPermissionToRole(Role role, Permission permission) {
        boolean exists = rolePermissionRepository.findAll().stream()
                .anyMatch(rp -> rp.getRole().getId().equals(role.getId()) &&
                        rp.getPermission().getId().equals(permission.getId()));
        if (!exists) {
            rolePermissionRepository.save(
                    RolePermission.builder().role(role).permission(permission).build()
            );
        }
    }
}
