package com.elite.employeemanager.auth.user.entity;

import com.elite.employeemanager.auth.mapping.entity.UserRole;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime passwordLastUpdatedAt=LocalDateTime.now();

    @Builder.Default
    @Column(nullable = false)
    private boolean isActive=true;

    private LocalDateTime lastLogin;

    @Builder.Default
    @Column(nullable = false)
    private Integer failedLoginAttempts=0;
    
    private LocalDateTime accountLockedUntil;

    @Builder.Default
    @Column(nullable = false)
    private boolean forcePasswordChange=false;

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime createdAt=LocalDateTime.now();

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime updatedAt=LocalDateTime.now();

}
