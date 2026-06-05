package com.elite.employeemanager.auth.mapping.repository;

import com.elite.employeemanager.auth.mapping.entity.RoleComponent;
import com.elite.employeemanager.auth.role.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface RoleComponentRepository extends JpaRepository<RoleComponent,Long> {
    List<RoleComponent> findByRoleIn(Collection<Role> roles);
}
