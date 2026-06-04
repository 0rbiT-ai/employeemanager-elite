package com.elite.employeemanager.auth.mapping.repository;

import com.elite.employeemanager.auth.mapping.entity.RolePermission;
import com.elite.employeemanager.auth.role.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RolePermissionRepository extends JpaRepository<RolePermission,Long> {
    List<RolePermission> findByRole(Role role);
}
