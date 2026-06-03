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
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    private String passwordHash;

    @Builder.Default
    private LocalDateTime passwordLastUpdatedAt=LocalDateTime.now();

    @Builder.Default
    private boolean isActive=true;

    private LocalDateTime lastLogin;

    @Builder.Default
    private Integer failedLoginAttempts=0;

    @Builder.Default
    private LocalDateTime accountLockedUntil=null;

    @Builder.Default
    private boolean forcePasswordChange=false;

    @Builder.Default
    private LocalDateTime createdAt=LocalDateTime.now();

    @Builder.Default
    private LocalDateTime updatedAt=LocalDateTime.now();

    @OneToMany(mappedBy = "user")
    private List<UserRole> userRoles;
}
