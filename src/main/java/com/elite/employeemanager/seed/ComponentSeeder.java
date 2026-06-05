package com.elite.employeemanager.seed;

import com.elite.employeemanager.auth.component.entity.Component;
import com.elite.employeemanager.auth.component.repository.ComponentRepository;
import com.elite.employeemanager.auth.mapping.entity.RoleComponent;
import com.elite.employeemanager.auth.mapping.repository.RoleComponentRepository;
import com.elite.employeemanager.auth.role.entity.Role;
import com.elite.employeemanager.auth.role.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;

import java.time.LocalDateTime;


@org.springframework.stereotype.Component
@RequiredArgsConstructor
@Order(4)
public class ComponentSeeder implements CommandLineRunner {

    private final ComponentRepository componentRepository;
    private final RoleRepository roleRepository;
    private final RoleComponentRepository roleComponentRepository;

    @Override
    public void run(String... args) throws Exception {
        // 1. Seed Navigation Components
        Component dashboard = seedComponent("DASHBOARD", "Dashboard", "/dashboard", "layout-dashboard", "SHARED", 1);
        Component adminOverview = seedComponent("ADMIN_OVERVIEW", "Admin Overview", "/admin/overview", "shield-alert", "ADMIN", 2);
        Component timesheets = seedComponent("TIMESHEET_SUBMIT", "Timesheets", "/timesheets", "clock", "SHARED", 3);
        Component meetings = seedComponent("MEETINGS_VIEW", "Link Room", "/meetings", "video", "SHARED", 4);
        Component employees = seedComponent("EMPLOYEES", "Employees Directory", "/admin/employees", "users", "ADMIN", 5);
        // 2. Fetch Roles
        Role admin = roleRepository.findByRoleCode("ADMIN").orElseThrow();
        Role teamLead = roleRepository.findByRoleCode("TEAM_LEAD").orElseThrow();
        Role employee = roleRepository.findByRoleCode("EMPLOYEE").orElseThrow();
        // 3. Map Component Visibility and CRUD access to Roles
        // Admin gets access to all
        mapComponentToRole(admin, dashboard, true, true, true, true);
        mapComponentToRole(admin, adminOverview, true, true, true, true);
        mapComponentToRole(admin, timesheets, true, true, true, true);
        mapComponentToRole(admin, meetings, true, true, true, true);
        mapComponentToRole(admin, employees, true, true, true, true);
        // Team Leads get dashboard, timesheets, and meetings
        mapComponentToRole(teamLead, dashboard, true, true, true, true);
        mapComponentToRole(teamLead, timesheets, true, true, true, true);
        mapComponentToRole(teamLead, meetings, true, true, true, true);
        // Employees get dashboard, timesheets, and meetings (but view/edit only, no delete)
        mapComponentToRole(employee, dashboard, true, false, false, false);
        mapComponentToRole(employee, timesheets, true, true, true, false);
        mapComponentToRole(employee, meetings, true, false, false, false);
    }

    private Component seedComponent(String key, String name, String route, String icon, String portal, int order) {
        return componentRepository.findByComponentKey(key)
                .orElseGet(() -> componentRepository.save(
                        Component.builder()
                                .componentKey(key)
                                .componentName(name)
                                .routePath(route)
                                .icon(icon)
                                .portal(portal)
                                .displayOrder(order)
                                .isActive(true)
                                .createdAt(LocalDateTime.now())
                                .build()
                ));
    }

    private void mapComponentToRole(Role role, Component component, boolean view, boolean create, boolean edit, boolean delete) {
        boolean exists = roleComponentRepository.findAll().stream()
                .anyMatch(rc -> rc.getRole().getId().equals(role.getId()) &&
                        rc.getComponent().getId().equals(component.getId()));
        if (!exists) {
            roleComponentRepository.save(
                    RoleComponent.builder()
                            .role(role)
                            .component(component)
                            .canView(view)
                            .canCreate(create)
                            .canEdit(edit)
                            .canDelete(delete)
                            .createdAt(LocalDateTime.now())
                            .build()
            );
        }
    }
}
