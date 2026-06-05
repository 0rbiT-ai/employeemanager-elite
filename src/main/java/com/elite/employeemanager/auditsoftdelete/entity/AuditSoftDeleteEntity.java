package com.elite.employeemanager.auditsoftdelete.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/*

use @SQLDelete and @SQLRestriction annotations in child entities like ->

@SQLDelete(sql = "UPDATE employees SET is_deleted = true, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("is_deleted = false")

 */

@Getter
@Setter
@MappedSuperclass
public abstract class AuditSoftDeleteEntity extends AuditableEntity{

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private Long deletedBy;

    @Column(name = "delete_reason")
    private String deleteReason;
}
