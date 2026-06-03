package com.elite.employeemanager.auth.mapping.entity;

import com.elite.employeemanager.auth.permissions.entity.Permissions;
import com.elite.employeemanager.auth.role.entity.Role;
import jakarta.persistence.*;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "role_permissions")
public class RolePermissions {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id",nullable = false)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permissions_id",nullable = false)
    private Permissions permissions;
}
