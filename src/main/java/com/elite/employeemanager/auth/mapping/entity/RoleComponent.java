package com.elite.employeemanager.auth.mapping.entity;

import com.elite.employeemanager.auth.component.entity.Component;
import com.elite.employeemanager.auth.role.entity.Role;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "role_components")
public class RoleComponent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id",nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "component_id",nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Component component;

    @Builder.Default
    @Column(nullable = false)
    private Boolean canView=true;

    @Builder.Default
    @Column(nullable = false)
    private Boolean canCreate=false;

    @Builder.Default
    @Column(nullable = false)
    private Boolean canEdit=false;

    @Builder.Default
    @Column(nullable = false)
    private Boolean canDelete=false;

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime createdAt=LocalDateTime.now();
}
