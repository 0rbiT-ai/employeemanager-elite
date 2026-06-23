package com.elite.employeemanager.timesheet.entity;

import com.elite.employeemanager.employee.entity.Employee;
import com.elite.employeemanager.project.entity.Project;
import com.elite.employeemanager.task.entity.Task;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Check(constraints = "work_category in ('FEATURE', 'BUG', 'STORY', 'RND', 'CRC', 'COC', 'SUPPORT', 'TASK', 'OTHER', 'MEETING', 'ADMIN') AND status in ('PENDING', 'APPROVED', 'REJECTED')")
@Table(name = "timesheet_entries") // uniqueConstraints = @UniqueConstraint(columnNames = {"employee_id","start_time"})
public class TimesheetEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id",nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    private String bugNumber;

    @Column(nullable = false)
    private String workCategory;

    @Column(nullable = false)
    private LocalDate workDate;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Column(nullable = false, precision = 4, scale = 2)
    private BigDecimal hoursSpent;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String status; // default pending

    @Column(columnDefinition = "TEXT")
    private String justification;

    @Column(columnDefinition = "TEXT")
    private String managerComment;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt; // default now time
}
