package com.elite.employeemanager.employee.entity;

import com.elite.employeemanager.auditsoftdelete.entity.AuditSoftDeleteEntity;
import com.elite.employeemanager.auth.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Check(constraints = "status in ('ACTIVE','INACTIVE', 'ON_LEAVE') AND notification_preference in ('EMAIL', 'WHATSAPP', 'TEAMS', 'ALL')")
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

    private String personalEmail;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String designation;

    @Transient
    private List<String> roles;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    private User user;

    @Column(nullable = false)
    private LocalDate joiningDate;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private String notificationPreference;

    private String profileImage;

}
