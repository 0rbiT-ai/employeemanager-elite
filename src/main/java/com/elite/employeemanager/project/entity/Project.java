package com.elite.employeemanager.project.entity;

import com.elite.employeemanager.auditsoftdelete.entity.AuditSoftDeleteEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import java.time.LocalDate;


@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Check(constraints = "status in ('ACTIVE', 'ON_HOLD', 'COMPLETED', 'CANCELLED') AND progress_percentage>=0 AND progress_percentage<=100 AND color_hex ~ '^#[A-Fa-f0-9]{6}$'")
@Table(name = "projects")
@SQLDelete(sql = "UPDATE projects SET is_deleted = true, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("is_deleted = false")
public class Project extends AuditSoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String projectName;

    private String description;

    @Column(nullable = false)
    private String clientName;

    @Column(length = 7,nullable = false)
    @Pattern(regexp = "^#[A-Fa-f0-9]{6}$", message = "color must be a valid hex code")
    private String colorHex;

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate;

    @Column(nullable = false)
    private String status;

    @Min(0)
    @Max(100)
    @Column(nullable = false)
    private Integer progressPercentage;

}
