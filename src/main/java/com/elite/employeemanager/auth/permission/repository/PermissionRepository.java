package com.elite.employeemanager.auth.permission.repository;

import com.elite.employeemanager.auth.permission.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<Permission,Long> {
}
