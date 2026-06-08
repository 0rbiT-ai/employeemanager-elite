package com.elite.employeemanager.auditsoftdelete.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @JsonIgnore
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private Long deletedBy;

    @Column(name = "delete_reason")
    private String deleteReason;
}
