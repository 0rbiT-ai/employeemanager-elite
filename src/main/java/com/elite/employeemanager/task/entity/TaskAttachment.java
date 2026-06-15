package com.elite.employeemanager.task.entity;

import com.elite.employeemanager.employee.entity.Employee;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "task_attachments")
public class TaskAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id",nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Task task;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false,length = 500)
    private String filePath;

    @Column(nullable = false, name = "file_size_bytes")
    private Long fileSizeBytes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by",nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Employee uploadedBy;

    @Column(nullable = false,updatable = false)
    private LocalDateTime uploadedAt;

}
