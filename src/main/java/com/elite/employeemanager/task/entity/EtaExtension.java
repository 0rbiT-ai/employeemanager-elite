package com.elite.employeemanager.task.entity;

import com.elite.employeemanager.auth.user.entity.User;
import com.elite.employeemanager.employee.entity.Employee;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Check(constraints = "status in ('PENDING', 'APPROVED', 'REJECTED')")
@Table(name = "eta_extension_requests")
public class EtaExtension {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id",nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_by",nullable = false)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    private Employee requestedBy;

    @Column(nullable = false)
    private LocalDate oldEtaDate;

    @Column(nullable = false)
    private LocalDate newEtaDate;

    @Column(columnDefinition = "TEXT",nullable = false)
    private String reason;

    @Column(nullable = false)
    private String status; // default pending

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    private LocalDateTime reviewedAt;

    @Column(columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(nullable = false)
    private LocalDateTime createdAt; // default time now

}
