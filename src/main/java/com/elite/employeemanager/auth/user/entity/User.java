package com.elite.employeemanager.auth.user.entity;

import com.elite.employeemanager.auth.mapping.entity.UserRole;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Transient
    @JsonProperty(value = "password", access = JsonProperty.Access.WRITE_ONLY)
    private String rawPassword;

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime passwordLastUpdatedAt=LocalDateTime.now();

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive=true;

    private LocalDateTime lastLogin;

    @Builder.Default
    @Column(nullable = false)
    private Integer failedLoginAttempts=0;
    
    private LocalDateTime accountLockedUntil;

    @Builder.Default
    @Column(nullable = false)
    private Boolean forcePasswordChange=false;

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime createdAt=LocalDateTime.now();

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime updatedAt=LocalDateTime.now();

    @Transient
    private Collection<? extends GrantedAuthority> authorities;

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return accountLockedUntil==null || accountLockedUntil.isBefore(LocalDateTime.now());
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return isActive != null && isActive;
    }
}
