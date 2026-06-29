package com.elite.employeemanager.team.entity;

import com.elite.employeemanager.auditsoftdelete.entity.AuditSoftDeleteEntity;
import com.elite.employeemanager.employee.entity.Employee;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.*;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Check(constraints = "status in ('ACTIVE','INACTIVE')")
@Table(name = "teams")
@SQLDelete(sql = "UPDATE teams SET is_deleted = true, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("is_deleted = false")
public class Team extends AuditSoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String teamName;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lead_id")
    @OnDelete(action = OnDeleteAction.RESTRICT)
    private Employee lead;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_lead_id")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Employee subLead;

    @Column(name = "teams_channel_id")
    private String teamsChannelId;

    @Column(name = "teams_group_id")
    private String teamsGroupId;

    @Column(nullable = false)
    private String status;

}
