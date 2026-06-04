package com.elite.employeemanager.auth.permission.repository;

import com.elite.employeemanager.auth.permission.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PermissionRepository extends JpaRepository<Permission,Long> {
    Optional<Permission> findByPermissionName(String name);
}
