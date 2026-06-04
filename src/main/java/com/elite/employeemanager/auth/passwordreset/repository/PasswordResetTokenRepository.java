package com.elite.employeemanager.auth.passwordreset.repository;

import com.elite.employeemanager.auth.passwordreset.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken,Long> {
}
