package com.elite.employeemanager.task.entity;

import com.elite.employeemanager.auditsoftdelete.entity.AuditSoftDeleteEntity;
import com.elite.employeemanager.employee.entity.Employee;
import com.elite.employeemanager.project.entity.Project;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Check(constraints = "task_type in ('FEATURE', 'BUG', 'STORY', 'RND', 'CRC', 'COC', 'SUPPORT', 'TASK', 'POC') AND priority in ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL') AND status in ('OPEN', 'IN_PROGRESS', 'PENDING_REVIEW', 'COMPLETED', 'OVER_ETA', 'TRANSFERRED', 'ETA_EXTENDED', 'REJECTED')")
@Table(name = "tasks")
@SQLDelete(sql = "UPDATE tasks SET is_deleted = true, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("is_deleted = false")
public class Task extends AuditSoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String taskNumber; // unique partial index

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id",nullable = false)
    private Project project;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(nullable = false)
    private String taskType;

    @Column(nullable = false)
    private String priority;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private BigDecimal etaHours;

    @Column(nullable = false)
    private LocalDate etaDate;

    @Column(nullable = false)
    private LocalDate originalEtaDate;

    private LocalDate extendedEtaDate;

    private String bugNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Employee assignedTo;

    private String epic;

}
