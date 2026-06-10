package com.elite.employeemanager.auth.passwordreset.repository;

import com.elite.employeemanager.auth.passwordreset.entity.PasswordResetToken;
import com.elite.employeemanager.auth.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken,Long> {
    Optional<PasswordResetToken> findByToken(String token);

    void deleteByUser(User user);
}
