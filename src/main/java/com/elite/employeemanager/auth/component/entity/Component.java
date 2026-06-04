package com.elite.employeemanager.auth.component.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "components")
public class Component {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true,nullable = false)
    private String componentKey;

    @Column(nullable = false)
    private String componentName;

    @Column(nullable = false)
    private String routePath;

    private String icon;

    @Column(nullable = false)
    private String portal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_component_id", nullable = true)
    private Component parentComponent;

    @Builder.Default
    @Column(nullable = false)
    private Integer displayOrder=0;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isActive=true;

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime createdAt=LocalDateTime.now();
}
