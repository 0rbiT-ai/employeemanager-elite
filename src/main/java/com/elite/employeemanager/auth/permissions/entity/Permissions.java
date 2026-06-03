package com.elite.employeemanager.auth.permissions.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users")
public class Permissions {
    @Id
    @GeneratedValue
    private Long id;

    private String permissionName;

    private String description;
}
