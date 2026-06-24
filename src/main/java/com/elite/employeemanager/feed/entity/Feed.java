package com.elite.employeemanager.feed.entity;

import com.elite.employeemanager.employee.entity.Employee;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Check;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Check(constraints = "severity in ('INFO','WARNING','CRITICAL','SUCCESS') and (teams_post_status is null or teams_post_status in ('PENDING','SUCCESS','FAILED'))")
@Table(name = "feed")
public class Feed {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private String severity;

    @Column(nullable = false)
    private Boolean publishToInternal; // default false

    @Column(nullable = false)
    private Boolean publishToTeams; // default false

    private String teamsPostStatus;

    private String teamsMessageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by",nullable = false)
    private Employee createdBy;

    @Column(nullable = false)
    private LocalDateTime createdAt; // default time now

}
