package com.elite.employeemanager.auth.mapping.repository;

import com.elite.employeemanager.auth.mapping.entity.UserRole;
import com.elite.employeemanager.auth.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRoleRepository extends JpaRepository<UserRole,Long> {
    List<UserRole> findByUser(User user);
}
