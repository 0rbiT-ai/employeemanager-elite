package com.elite.employeemanager.team.entity;

import com.elite.employeemanager.employee.entity.Employee;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "team_members", uniqueConstraints = @UniqueConstraint(columnNames = {"team_id","employee_id"}))
public class TeamEmployee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id",nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id",nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Employee employee;

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime joinedAt=LocalDateTime.now();

}
