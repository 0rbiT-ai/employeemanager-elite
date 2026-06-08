package com.elite.employeemanager.employee.entity;

import com.elite.employeemanager.auditsoftdelete.entity.AuditSoftDeleteEntity;
import com.elite.employeemanager.auth.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "employees")
@SQLDelete(sql = "UPDATE employees SET is_deleted = true, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("is_deleted = false")
public class Employee extends AuditSoftDeleteEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String employeeCode;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String workEmail;

    @Column(nullable = false)
    private String personalEmail;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String designation;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    private User user;

    @Column(nullable = false)
    private LocalDate joiningDate;

    @Builder.Default
    @Column(nullable = false)
    @Check(constraints = "status in ('ACTIVE','INACTIVE', 'ON_LEAVE')")
    private String status="ACTIVE";

    @Builder.Default
    @Column(nullable = false)
    @Check(constraints = "notification_preference in ('EMAIL', 'WHATSAPP', 'TEAMS', 'ALL')")
    private String notificationPreference="ALL";

    private String profileImage;

}
