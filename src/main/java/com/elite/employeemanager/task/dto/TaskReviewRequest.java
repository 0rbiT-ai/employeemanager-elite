package com.elite.employeemanager.task.dto;

import lombok.Data;

@Data
public class TaskReviewRequest {
    private String status; // APPROVED or REJECTED
    private String comment;
}
