package com.elite.employeemanager.auth.role.entity;

import com.elite.employeemanager.auth.mapping.entity.RolePermissions;
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
@Table(name = "roles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(unique = true,nullable = false)
    private String roleCode;

    @Column(nullable = false)
    private String name;

    private String description;

    @Builder.Default
    private Boolean isActive = true;

    @Builder.Default
    private LocalDateTime createdAt=LocalDateTime.now();

    @OneToMany(mappedBy = "role")
    private List<RolePermissions> rolePermissions;
}
