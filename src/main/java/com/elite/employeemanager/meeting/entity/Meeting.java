package com.elite.employeemanager.meeting.entity;

import com.elite.employeemanager.auditsoftdelete.entity.AuditableEntity;
import com.elite.employeemanager.employee.entity.Employee;
import com.elite.employeemanager.project.entity.Project;
import com.elite.employeemanager.task.entity.Task;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "meetings")
public class Meeting extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "meeting_link", nullable = false)
    private String meetingLink;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Task task;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "meeting_attendees", joinColumns = @JoinColumn(name = "meeting_id"), inverseJoinColumns = @JoinColumn(name = "employee_id"))
    private Set<Employee> attendees;

}