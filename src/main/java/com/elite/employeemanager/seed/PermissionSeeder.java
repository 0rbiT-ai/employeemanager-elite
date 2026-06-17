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

        //seed permissions
        Permission userView = seedPermission("USER_VIEW", "View system user accounts");
        Permission userCreate = seedPermission("USER_CREATE", "Create system user credentials");
        Permission userUpdate = seedPermission("USER_UPDATE", "Update system user accounts");
        Permission userDelete = seedPermission("USER_DELETE", "Delete system user accounts");

        Permission employeeView = seedPermission("EMPLOYEE_VIEW", "View employee records");
        Permission employeeCreate = seedPermission("EMPLOYEE_CREATE", "Create employee records");
        Permission employeeUpdate = seedPermission("EMPLOYEE_UPDATE", "Update employee records");
        Permission employeeDelete = seedPermission("EMPLOYEE_DELETE", "Delete employee records");

        Permission teamView = seedPermission("TEAM_VIEW", "View team records");
        Permission teamCreate = seedPermission("TEAM_CREATE", "Create teams");
        Permission teamUpdate = seedPermission("TEAM_UPDATE", "Update team details");
        Permission teamDelete = seedPermission("TEAM_DELETE", "Delete teams");

        Permission projectView = seedPermission("PROJECT_VIEW", "View project records");
        Permission projectCreate = seedPermission("PROJECT_CREATE", "Create projects");
        Permission projectUpdate = seedPermission("PROJECT_UPDATE", "Update project details");
        Permission projectDelete = seedPermission("PROJECT_DELETE", "Delete projects");

        Permission taskView = seedPermission("TASK_VIEW", "View task records");
        Permission taskCreate = seedPermission("TASK_CREATE", "Create tasks");
        Permission taskUpdate = seedPermission("TASK_UPDATE", "Update task details");
        Permission taskDelete = seedPermission("TASK_DELETE", "Delete tasks");

        // 2. Fetch Roles
        Role admin = roleRepository.findByRoleCode("ADMIN").orElseThrow();
        Role teamLead = roleRepository.findByRoleCode("TEAM_LEAD").orElseThrow();
        Role subLead = roleRepository.findByRoleCode("SUB_LEAD").orElseThrow();
        Role employee = roleRepository.findByRoleCode("EMPLOYEE").orElseThrow();

        // 3. Map Permissions to Roles
        // ==================== ADMIN ====================
        mapPermissionToRole(admin, userView);
        mapPermissionToRole(admin, userCreate);
        mapPermissionToRole(admin, userUpdate);
        mapPermissionToRole(admin, userDelete);

        mapPermissionToRole(admin, employeeView);
        mapPermissionToRole(admin, employeeCreate);
        mapPermissionToRole(admin, employeeUpdate);
        mapPermissionToRole(admin, employeeDelete);

        mapPermissionToRole(admin, teamView);
        mapPermissionToRole(admin, teamCreate);
        mapPermissionToRole(admin, teamUpdate);
        mapPermissionToRole(admin, teamDelete);

        mapPermissionToRole(admin, projectView);
        mapPermissionToRole(admin, projectCreate);
        mapPermissionToRole(admin, projectUpdate);
        mapPermissionToRole(admin, projectDelete);

        mapPermissionToRole(admin, taskView);
        mapPermissionToRole(admin, taskCreate);
        mapPermissionToRole(admin, taskUpdate);
        mapPermissionToRole(admin, taskDelete);


// ==================== TEAM LEAD ====================

        mapPermissionToRole(teamLead, employeeView);

        mapPermissionToRole(teamLead, teamView);
        mapPermissionToRole(teamLead, teamUpdate);

        mapPermissionToRole(teamLead, projectView);
        mapPermissionToRole(teamLead, projectCreate);
        mapPermissionToRole(teamLead, projectUpdate);

        mapPermissionToRole(teamLead, taskView);
        mapPermissionToRole(teamLead, taskCreate);
        mapPermissionToRole(teamLead, taskUpdate);
        mapPermissionToRole(teamLead, taskDelete);


// ==================== SUB LEAD ====================

        mapPermissionToRole(subLead, employeeView);

        mapPermissionToRole(subLead, teamView);
        mapPermissionToRole(subLead, teamUpdate);

        mapPermissionToRole(subLead, projectView);
        mapPermissionToRole(subLead, projectCreate);
        mapPermissionToRole(subLead, projectUpdate);

        mapPermissionToRole(subLead, taskView);
        mapPermissionToRole(subLead, taskCreate);
        mapPermissionToRole(subLead, taskUpdate);
        mapPermissionToRole(subLead, taskDelete);


// ==================== EMPLOYEE ====================

        mapPermissionToRole(employee, employeeView);

        mapPermissionToRole(employee, teamView);

        mapPermissionToRole(employee, projectView);

        mapPermissionToRole(employee, taskView);
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
