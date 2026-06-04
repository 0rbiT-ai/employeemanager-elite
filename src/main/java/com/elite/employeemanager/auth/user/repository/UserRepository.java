package com.elite.employeemanager.auth.user.repository;

import com.elite.employeemanager.auth.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByEmail(String email);

    String email(String email);
}
