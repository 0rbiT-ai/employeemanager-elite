package com.elite.employeemanager.task.entity;

import com.elite.employeemanager.auditsoftdelete.entity.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Check;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EqualsAndHashCode(callSuper = true)
@Table(name = "task_draft_batch")
@Check(constraints = "status in ('OPEN','PUBLISHED','DISCARDED','REMINDED')")
public class TaskDraftBatch extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String status;

    @Lob
    @Column(name = "items_json", nullable = false)
    private String teamsMessage;

    private LocalDateTime reminderSentAt;
}
