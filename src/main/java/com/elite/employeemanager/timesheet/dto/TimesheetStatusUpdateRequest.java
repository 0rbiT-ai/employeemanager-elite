package com.elite.employeemanager.timesheet.dto;

import lombok.Data;

@Data
public class TimesheetStatusUpdateRequest {
    private String status;
    private String managerComment;
}
