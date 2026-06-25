package com.elite.employeemanager.link.entity;

import com.elite.employeemanager.auditsoftdelete.entity.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

@Data
@Builder
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "links")
public class Link extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "filename", nullable = false)
    private String filename;

    @Column(name = "filelink", nullable = false)
    private String filelink;

}
